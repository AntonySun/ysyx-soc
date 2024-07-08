package ysyx

import chisel3._
import chisel3.util._

import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

class SPIIO(val ssWidth: Int = 8) extends Bundle {
  val sck = Output(Bool())
  val ss = Output(UInt(ssWidth.W))
  val mosi = Output(Bool())
  val miso = Input(Bool())
}

class spi_top_apb extends BlackBox {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Reset())
    val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
    val spi = new SPIIO
    val spi_irq_out = Output(Bool())
  })
}

class flash extends BlackBox {
  val io = IO(Flipped(new SPIIO(1)))
}

class APBSPI(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
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
    val spi_bundle = IO(new SPIIO)

    val mspi = Module(new spi_top_apb)
    mspi.io.clock := clock
    mspi.io.reset := reset
    spi_bundle <> mspi.io.spi

    val sIdle :: setup :: enable :: setread :: readen :: Nil = Enum(5)
    val state = RegInit(sIdle)
    val count = RegInit(0.U(4.W))
    val rdata = RegInit(0.U(32.W))
    val pprot = RegInit(1.U(3.W))
    val penable = RegInit(false.B)
    val pwrite = RegInit(false.B)
    val paddr = RegInit(0.U(32.W))
    val pwdata = RegInit(0.U(32.W))
    val psel = RegInit(false.B)
    val pstrb = RegInit(0.U(4.W))
    val in_addr = RegInit(0.U(32.W))
   //printf("the xip state is %d\n", state) 
    when (in.paddr >= "h30000000".U(32.W) && in.paddr <= "h3fffffff".U(32.W)) {
      in.prdata := Cat(mspi.io.in.prdata(7, 0), Fill(24, 0.U)) | Cat(Fill(8, 0.U), mspi.io.in.prdata(15, 8), Fill(16, 0.U)) | Cat(Fill(16, 0.U), mspi.io.in.prdata(23, 16), Fill(8, 0.U)) | Cat(Fill(24, 0.U), mspi.io.in.prdata(31, 24))
      in.pready := 0.U
      in.pslverr := 0.U
      mspi.io.in.pprot := pprot
      mspi.io.in.penable := penable
      mspi.io.in.pwrite := pwrite
      mspi.io.in.paddr := paddr
      mspi.io.in.pwdata := pwdata
      mspi.io.in.psel := psel
      mspi.io.in.pstrb := pstrb
     // count := 0.U
      switch (state) {
        is(sIdle) {
          pprot := 1.U
          penable := false.B
          pwrite := false.B
          paddr := 0.U
          pwdata := 0.U
          psel := false.B
          pstrb := 0.U
          
           in.pready := 1.U
          
          when (in_addr =/= in.paddr && ((in.paddr >= "h30000000".U(32.W) && in.paddr <= "h3fffffff".U(32.W)))) {
            in.pready := 0.U
            count := 0.U
            state := setup
          }.otherwise{
            state := sIdle
            /*when (count === 6.U) {
              in.pready := 1.U
            }.otherwise {
              in.pready := 0.U
            }*/
          }
        }
        is(setup) {
          in_addr := in.paddr
          count := count + 1.U
          penable := false.B
          pwrite := true.B
          paddr :=  Mux(count === 0.U, "h10001014".U(32.W), Mux(count === 1.U, "h10001018".U(32.W), Mux(count === 2.U, "h10001004".U(32.W), Mux(count === 3.U, "h10001010".U(32.W), 0.U))))
          pwdata := Mux(count === 0.U, "h00000018".U(32.W), Mux(count === 1.U, "h00000001".U(32.W), Mux(count === 2.U, Cat("h03".U(8.W), in.paddr(23, 0)), Mux(count === 3.U , "h00002140".U(32.W),  0.U))))
          psel := true.B
          pstrb := Mux(count === 0.U, 3.U, Mux(count === 1.U, 1.U, Mux(count === 2.U, 15.U, Mux(count === 3.U, 15.U, 0.U))))
          state := Mux(count < 4.U, enable, setread)
          //printf("the xip count is %d\n", count)
          //printf("the in.pstrb is %d\n", in.pstrb)
          
        }
        is(enable) {
         // printf("the xip pwdata is 0x%x\n", pwdata)
          penable := 1.U
          when (mspi.io.in.pready === true.B) {
            
            penable := false.B
            state := Mux(count < 4.U, setup, setread)

          }.otherwise {
            state := enable
          }
          
          //count := Mux(mspi.io.in.pready === false.B && penable === false.B, count, count + 1.U)
          //penable := Mux(mspi.io.in.pready === false.B && penable === false.B, true.B, false.B)
          //state := Mux(mspi.io.in.pready === false.B && penable === false.B, enable, Mux(count < 4.U, setup, setread))
        }
        is(setread) {

          penable := false.B
          pwrite := false.B
          paddr := Mux(count === 4.U, "h10001010".U(32.W), Mux(count === 5.U, "h10001000".U(32.W), 0.U))
          psel := true.B
          pstrb := 0.U
          state := readen
          //count := count + 1.U
        }
        is (readen) {
          //count := count + 1.U
          when (count === 4.U) {
            penable := 1.U
            when (mspi.io.in.pready === true.B) {
              penable := 0.U
              count := Mux(((mspi.io.in.prdata & "h00000100".U(32.W)) === 0.U(32.W)), count + 1.U, 4.U)
              state := setread
            }
            //data := mspi.io.in.prdata
            
            
          }.otherwise {
            penable := 1.U
            when (mspi.io.in.pready === true.B) {
              penable := 0.U
              count := count + 1.U
              
              rdata := mspi.io.in.prdata
              
              
              state := sIdle 
            }
           
          }
        }
      }
    }.otherwise {
      mspi.io.in <> in
    }

  }
}
