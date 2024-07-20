package ysyx

import chisel3._
import chisel3.util._
import chisel3.experimental.Analog

import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

class SDRAMIO extends Bundle {
  val clk = Output(Bool())
  val cke = Output(Bool())
  val cs  = Output(Bool())
  val ras = Output(Bool())
  val cas = Output(Bool())
  val we  = Output(Bool())
  val a   = Output(UInt(13.W))
  val ba  = Output(UInt(2.W))
  val dqm = Output(UInt(2.W))
  val dq  = Analog(16.W)
}

class sdram_top_axi extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val in = Flipped(new AXI4Bundle(AXI4BundleParameters(addrBits = 32, dataBits = 32, idBits = 4)))
    val sdram = new SDRAMIO
  })
}

class sdram_top_apb extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
    val sdram = new SDRAMIO
  })
}

class sdram extends BlackBox {
  val io = IO(Flipped(new SDRAMIO))
}

class sdram_write extends BlackBox with HasBlackBoxInline  {
  val io = IO(new Bundle {
    val waddr = Input(UInt(32.W))
    val clk = Input(Bool())
    val wdata = Input(UInt(16.W))
    val scount = Input(UInt(8.W))
    val dqm = Input(UInt(2.W))
    val wenable = Input(Bool())
  })
  setInline("sdram_write.v",
  """module sdram_write(
    | input [31:0] waddr,
    | input clk,
    | input [15:0] wdata,
    | input [7:0] scount,
    | input [1:0] dqm,
    | input wenable
    |); 
    |import "DPI-C" function void sdram_write(input int waddr, input int wdata, input int dqm, input int scount);
    |always @(posedge clk) begin
    |  if (wenable) sdram_write(waddr, {16'b0, wdata}, {30'b0,dqm}, {24'b0, scount});
    |  
    |end
    |endmodule
  """.stripMargin)
}

class  sdram_read extends BlackBox with HasBlackBoxInline {
  val io = IO(new Bundle {
    val raddr = Input(UInt(32.W))
    val rdata = Output(UInt(16.W))
    val clk = Input(Bool())
    val renable = Input(Bool()) 
  })
  setInline("sdram_read.v",
  """module sdram_read(
    | input [31:0] raddr,
    | input renable,
    | input clk,
    | output  reg [15:0] rdata
    |); 
    |import "DPI-C" function void sdram_read(input int raddr ,output int rdata);
    |reg [15:0] rdata_next;
    |always @(posedge clk) begin
    |  if (renable) begin
    |    sdram_read(raddr, {16'b0, rdata_next});
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

class sdramChisel extends RawModule {
  val io = IO(Flipped(new SDRAMIO))
  
  //val nop :: active :: write :: read :: precharge :: refresh :: loadmd :: Nil(7)
  val nop = 7.U(4.W)
  val active = 3.U(4.W)
  val read = 5.U(4.W)
  val write = 4.U(4.W)
  val burst_terminate = 6.U(4.W)
  val precharge = 2.U(4.W)
  val auto_refresh = 1.U(4.W)
  val load_mr = 0.U(4.W)

  //val state = RegInit(sIdle)
  
  val cmd = Cat(io.cs.asUInt, io.ras.asUInt, io.cas.asUInt, io.we.asUInt)
  
  val count = withClockAndReset((~io.clk).asClock, io.cs.asAsyncReset){RegInit(0.U(7.W))}
  val cas_count = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(7.W))}
  val scount = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(7.W))}

  //val addr = RegInit(0.U(32.W))
  
  val burstlen = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(3.W))}
  val bt = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(1.W))}
  val caslate = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(3.W))}
  val opmode = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(2.W))}
  
 

  //val addr = io.a 

  val waddr =  withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(32.W))}
  val wdata1 = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(16.W))}
  val wdata2 = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(16.W))}
  val pre_waddr = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(32.W))}

  val row_addr = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(13.W))}
  val ba_addr = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(2.W))}

  val r_count = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(7.W))}
  val raddr = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(32.W))}
  val t_enable = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(false.B)}
  val is_read = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(false.B)}

  val sdram_rdata = withClockAndReset(io.clk.asClock, io.cs.asAsyncReset){RegInit(0.U(16.W))}
  val dq = TriStateInBuf(io.dq, sdram_rdata, t_enable === true.B)
  val sdw = Module(new sdram_write)
  sdw.io.wenable := Mux(((count > 0.U ) || cmd === write) && (burstlen >= 0.U && burstlen <= 3.U), io.clk, 0.U)
  sdw.io.dqm := io.dqm
  sdw.io.wdata := dq
  sdw.io.waddr := Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U) / 2.U
  sdw.io.scount := Mux(cmd === write, 1.U, scount)
  sdw.io.clk := io.clk

  val sdr = Module(new sdram_read)
  sdr.io.renable := Mux(r_count > 0.U && cas_count === 0.U && cmd =/= burst_terminate, io.clk, 0.U)
  sdr.io.raddr := raddr
  sdr.io.clk := io.clk
  //wdata := io.dq
  /*when (cmd ===write) {
    wdata1 := io.dq
  }.elsewhen(cmd === nop && count > 0.U) {
    wdata2 := io.dq
  }.otherwise {
    wdata1 := wdata1
    wdata2 := wdata2
  }
  
  when(cmd === write && count === 0.U) {
    count := burstlen - 1.U
    when (addr_count === 0.U) {
      waddr := Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U)
    }.elsewhen (waddr === Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U)) {
      //waddr := Mux(io.dqm === 0.U, Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U) + 2.U, Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U)+ addr_count)
      waddr := waddr
      addr_count := Mux(io.dqm =/= 3.U, addr_count + 1.U, addr_count)
    }.elsewhen (waddr =/= Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U)) {
      waddr := Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U)
      addr_count := 1.U
    }
    
  }.elsewhen (count > 0.U && (cmd =/= burst_terminate)) {
    addr_count := Mux(io.dqm =/= 3.U, addr_count + 1.U, addr_count)
    count := count - 1.U

  }.otherwise {
    addr_count := addr_count
    count := 0
    waddr := waddr
  }*/

  when (cmd === read) {
    r_count := Mux(burstlen === 0.U, 1.U, Mux(burstlen === 1.U, 2.U, Mux(burstlen === 2.U, 4.U, Mux(burstlen === 3.U, 8.U, 0.U))))
    cas_count := caslate - 1.U
    raddr := Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U) / 2.U
    is_read := true.B

  }.elsewhen(cmd === nop && cas_count > 0.U) {
    
    cas_count := cas_count - 1.U
  }.elsewhen(r_count > 0.U && cas_count === 0.U && cmd =/= burst_terminate) {
     r_count := r_count - 1.U
    t_enable := true.B
    raddr := raddr + 2.U
    sdram_rdata := sdr.io.rdata
    when (r_count === 1.U) {
      is_read := false.B
      t_enable := false.B
    }
  }.otherwise {
    r_count := 0.U
    cas_count := 0.U
    is_read := false.B
    t_enable := false.B
  }

  when(cmd === write && count === 0.U) {
    count := Mux(burstlen === 0.U, 1.U, Mux(burstlen === 1.U, 2.U, Mux(burstlen === 2.U, 4.U, Mux(burstlen === 3.U, 8.U, 0.U))))
    scount := 2.U
    
  }.elsewhen (count > 0.U && (cmd =/= burst_terminate)) {
    scount := scount + 1.U
    count := count - 1.U

  }.otherwise {
    scount := 1.U
    count := 0.U   
  }

  when (cmd === load_mr) {
    burstlen := io.a(2, 0)
    bt := io.a(3)
    caslate := io.a(6, 4)
    opmode := io.a(8, 7)
  }.otherwise{
    burstlen := burstlen
    bt := bt
    caslate := caslate
    opmode := opmode
  }
  when (cmd === active) {
    row_addr := io.a
    ba_addr := io.ba
  }.otherwise {
    row_addr := row_addr
    ba_addr := ba_addr
  }
  /*  switch (state) {
    is (nop) {
      when (cmd === "b0111".U(4.W) || cmd === "b0010".U(4.W) || cmd === "b0001".U(4.W)) {
        state := sIdle
      }.elsewhen (cmd === 0.U) {
        state := loadmd
      }.elsewhen (cmd === 4.U) {
        count := count + 1.U
        addr := Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U)
        state := write
      }
    }
    is (loadmd) {
      burstlen := io.a(2, 0)
      bt := io.a(3)
      caslate := io.a(6, 4)
      opmode := io.a(8, 7)
      state := nop
    }
    is (write) {
      waddr := Mux(count === 1.U, Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U), Cat(Fill(8, 0.U), row_addr, ba_addr, io.a, 0.U) + count -1`)
      wdata := Mux(io.dqm === 3.U, 0.U, Mux(io.dqm === 2.U, Cat(Fill(24, 0.U), io.dq(7, 0)), Mux(io.dqm === 1.U, Cat(Fill(24, 0.U), io.dq(15, 8)), Cat(Fill(16, 0.U), io.dq))))

    }
    is (active) {
      row_addr := io.a
      ba_addr := io.ba
      state := nop
    }
  }*/

}

class AXI4SDRAM(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
  val beatBytes = 8
  val node = AXI4SlaveNode(Seq(AXI4SlavePortParameters(
    Seq(AXI4SlaveParameters(
        address       = address,
        executable    = true,
        supportsWrite = TransferSizes(1, beatBytes),
        supportsRead  = TransferSizes(1, beatBytes),
        interleavedId = Some(0))
    ),
    beatBytes  = beatBytes)))

  lazy val module = new Impl
  class Impl extends LazyModuleImp(this) {
    val (in, _) = node.in(0)
    val sdram_bundle = IO(new SDRAMIO)

    val converter = Module(new AXI4DataWidthConverter64to32)
    converter.io.clock := clock
    converter.io.reset := reset.asBool
    converter.io.in <> in

    val msdram = Module(new sdram_top_axi)
    msdram.io.clock := clock
    msdram.io.reset := reset.asBool
    msdram.io.in <> converter.io.out
    sdram_bundle <> msdram.io.sdram
  }
}

class APBSDRAM(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
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
    val sdram_bundle = IO(new SDRAMIO)

    val msdram = Module(new sdram_top_apb)
    msdram.io.clock := clock
    msdram.io.reset := reset.asBool
    msdram.io.in <> in
    sdram_bundle <> msdram.io.sdram
  }
}
