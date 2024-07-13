package ysyx

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog

import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._
import javax.swing.InputMap

class QSPIIO extends Bundle {
  val sck = Output(Bool())
  val ce_n = Output(Bool())
  val dio = Analog(4.W)
}

class psram_top_apb extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Reset())
    val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
    val qspi = new QSPIIO
  })
}

class psram extends BlackBox {
  val io = IO(Flipped(new QSPIIO))
}

class psram_write extends BlackBox with HasBlackBoxInline  {
  val io = IO(new Bundle {
    val waddr = Input(UInt(32.W))
    val wdata = Input(UInt(32.W))
    val count = Input(UInt(8.W))
    val wenable = Input(Bool())
  })
  setInline("psram_write.v",
  """module psram_write(
    | input [31:0] waddr,
    | input [31:0] wdata,
    | input [7:0] count,
    | input wenable
    |); 
    |import "DPI-C" function void ppsram_write(input int waddr, input int wdata, input int count);
    |always @(*) begin
    |  if (wenable) ppsram_write(waddr, wdata, {24'b0, count});
    |end
    |endmodule
  """.stripMargin)
}

class psram_read extends BlackBox with HasBlackBoxInline  {
  val io = IO(new Bundle {
    val raddr = Input(UInt(32.W))
    val rdata = Output(UInt(32.W))
    val renable = Input(Bool()) 
  })
  setInline("psram_read.v",
  """module psram_read(
    | input [31:0] raddr,
    | input renable,
    | output  reg [31:0] rdata
    |); 
    |import "DPI-C" function void ppsram_read(input int raddr ,output int rdata);
    |reg [31:0] rdata_next;
    |always @(*) begin
    |  if (renable) begin
    |    ppsram_read(raddr, rdata_next);
    |  end
    |  else  begin
    |     rdata_next = rdata;
    |end
    |end
    |
    |always @(posedge renable or negedge renable) begin
    |  if (renable) begin
    |    rdata <= rdata_next;
    |  end
    |end
    |endmodule
  """.stripMargin)
}

class psramChisel extends Module {
  val io = IO(Flipped(new QSPIIO))
  val Tenable = withClockAndReset(io.sck.asClock, io.ce_n.asAsyncReset){RegInit(false.B)}

  

  val cmd_addr :: writedata :: dummy :: sendread :: Nil = Enum(4)
  val state = withClockAndReset(io.sck.asClock, io.ce_n.asAsyncReset){RegInit(cmd_addr)}

  val cmd = withClockAndReset(io.sck.asClock, io.ce_n.asAsyncReset){RegInit(0.U(8.W))}
  val addr =  withClockAndReset(io.sck.asClock, io.ce_n.asAsyncReset){RegInit(0.U(24.W))}

  val waddr = Cat(Fill(8, 0.U), addr)
  val wwdata =  withClockAndReset(io.sck.asClock, io.ce_n.asAsyncReset){RegInit(0.U(32.W))}
  val count =  withClockAndReset(io.sck.asClock, io.ce_n.asAsyncReset){RegInit(0.U(8.W))}
  //val wenable = false.B

  val raddr = Cat(Fill(8, 0.U), addr)
  val rrdata =  withClockAndReset(io.sck.asClock, io.ce_n.asAsyncReset){RegInit(0.U(32.W))}
  val renable = withClockAndReset(io.sck.asClock, io.ce_n.asAsyncReset){RegInit(false.B)}
  val srdata = withClockAndReset(io.sck.asClock, io.ce_n.asAsyncReset){RegInit(0.U(32.W))}
  //val rising = withClockAndReset(io.ce_n.asClock, ((io.ce_n).asAsyncReset)){RegInit(false.B)}

  val di = TriStateInBuf(io.dio, srdata,  (Tenable === true.B) && (state === sendread)) // change this if you need

  val pw = Module(new psram_write)
  val pr = Module(new psram_read)
  pw.io.waddr := waddr
  pw.io.wdata := wwdata
  //pw.io.wenable := (state === writedata) && ((count === 16.U) || (count === 18.U) || (count === 22.U)) && (io.sck === false.B)
  pw.io.wenable := io.ce_n && !RegNext(io.ce_n) && (count =/= 0.U) && (count <= 22.U)
  pw.io.count := count
  pr.io.raddr := raddr
  rrdata := pr.io.rdata
  pr.io.renable := renable
  //rising := true.B

    switch(state) {
      
      is (cmd_addr) {
        when (io.ce_n === false.B) {
          when (count < 8.U) {
          //when(io.sck === 1.U) {
            cmd := (cmd << 1) | Cat(Fill(7, 0.U), di(0))
            count := count + 1.U
         //}
          
        }.elsewhen (count >= 8.U && count < 14.U) {
          //when(io.sck === 1.U) {
            addr := (addr << 4) | Cat(Fill(20, 0.U), di(3), di(2), di(1), di(0))
            count := count + 1.U
         // }
          
          when (count === 13.U) {
            when (cmd === "h38".U(8.W)) {
              state := writedata
            }.elsewhen (cmd === "heb".U(8.W)) {
              
              state := dummy
            }
          }
        }
      }.otherwise {
        state := cmd_addr
      }
        
      }
      is (writedata) {
        
          when (io.ce_n === false.B ) {
            when (count === 14.U) {
              wwdata := wwdata | Cat(Fill(24, 0.U), di, Fill(4, 0.U))
            }.elsewhen (count === 15.U) {
              wwdata := wwdata |  Cat(Fill(28, 0.U), di)
            }.elsewhen (count === 16.U) {
              wwdata := wwdata | Cat(Fill(16, 0.U), di, Fill(12, 0.U))
            }.elsewhen (count === 17.U) {
              wwdata := wwdata | Cat(Fill(20, 0.U), di, Fill(8, 0.U))
            }.elsewhen (count === 18.U) {
              wwdata := wwdata | Cat(Fill(8, 0.U), di, Fill(20, 0.U))
            }.elsewhen (count === 19.U) {
              wwdata := wwdata | Cat(Fill(12, 0.U), di, Fill(16, 0.U))
            }.elsewhen (count === 20.U) {
              wwdata := wwdata |Cat(di,Fill(28, 0.U))
            }.elsewhen (count === 21.U) {
              wwdata := wwdata | Cat(Fill(4, 0.U), di, Fill(24, 0.U))
            }
            count := count + 1.U
          }.otherwise {
           // rising := true.B
            //Tenable := 0.U
           // wenable := io.ce_n
            state := cmd_addr
          }
      }
      
      is (dummy) {
        //when (io.ce_n === false.B) {
          count := count + 1.U
          when (count === 18.U) {
            renable := true.B 
          }
          when (count === 19.U) {
          //count := count + 1.U
          
          Tenable := true.B 
          renable := false.B 
          state := sendread
        }
      //}
        
       
      }
      is (sendread) {
        
          count := count + 1.U
          
        when (count === 20.U) {
            srdata := rrdata(7, 4)
        }.elsewhen (count === 21.U) {
          srdata := rrdata(3, 0)
        }.elsewhen (count === 22.U) {
          srdata := rrdata(15, 12)
        }.elsewhen (count === 23.U) {
          srdata := rrdata(11, 8)
        }.elsewhen (count === 24.U) {
          srdata := rrdata(23, 20)
        }.elsewhen (count === 25.U) {
          srdata := rrdata(19, 16)
        }.elsewhen (count === 26.U) {
          srdata := rrdata(31, 28)
        }.elsewhen (count === 27.U) {

          srdata := rrdata(27, 24)
          //wenable := false.B
          
        }.elsewhen (count === 28.U) {
          state := cmd_addr
        }
        
        
      }
      //is (stop) {}
    }
  
}

class APBPSRAM(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
  val node = APBSlaveNode(Seq(APBSlavePortParameters(
    Seq(APBSlaveParameters(
      address       = address,
      executable    = true,
      supportsRead  = true,
      supportsWrite = true)),
    beatBytes  = 4)))

  lazy val module = new Impl
  class Impl extends LazyModuleImp(this) {
    val (in, _) = node.in(0)
    val qspi_bundle = IO(new QSPIIO)

    val mpsram = Module(new psram_top_apb)
    mpsram.io.clock := clock
    mpsram.io.reset := reset
    mpsram.io.in <> in
    qspi_bundle <> mpsram.io.qspi
  }
}
