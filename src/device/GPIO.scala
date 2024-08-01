package ysyx

import chisel3._
import chisel3.util._

import freechips.rocketchip.amba.apb._
import org.chipsalliance.cde.config.Parameters
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.util._

class GPIOIO extends Bundle {
  val out = Output(UInt(16.W))
  val in = Input(UInt(16.W))
  val seg = Output(Vec(8, UInt(8.W)))
}

class GPIOCtrlIO extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Reset())
  val in = Flipped(new APBBundle(APBBundleParameters(addrBits = 32, dataBits = 32)))
  val gpio = new GPIOIO
}

class gpio_top_apb extends BlackBox {
  val io = IO(new GPIOCtrlIO)
}

class gpioChisel extends Module {
  val io = IO(new GPIOCtrlIO)
  val led_reg = RegInit(0.U(16.W))
  val switch_reg = RegInit(0.U(16.W))
  val seg_reg = RegInit(0.U(32.W))
  io.gpio.out := led_reg
  switch_reg := io.gpio.in
  
  io.gpio.seg(0) := Mux(seg_reg(3, 0) === 0.U, "h03".U(8.W), Mux(seg_reg(3, 0) === 1.U, "h9f".U(8.W), Mux(seg_reg(3, 0) === 2.U, "h25".U(8.W), Mux(seg_reg(3, 0) === 3.U, "h0d".U(8.W), Mux(seg_reg(3, 0) === 4.U, "h99".U(8.W), Mux(seg_reg(3, 0) === 5.U, "h49".U(8.W), Mux(seg_reg(3, 0) === 6.U, "h41".U(8.W), Mux(seg_reg(3, 0) === 7.U, "h1f".U(8.W), Mux(seg_reg(3, 0) === 8.U, "h01".U(8.W), Mux(seg_reg(3, 0) === 9.U, "h09".U(8.W), Mux(seg_reg(3, 0) === 10.U, "h11".U(8.W), Mux(seg_reg(3, 0) === 11.U, "hc1".U(8.W), Mux(seg_reg(3, 0) === 12.U, "h63".U(8.W), Mux(seg_reg(3, 0) === 13.U, "h85".U(8.W), Mux(seg_reg(3, 0) === 14.U, "h61".U(8.W), Mux(seg_reg(3, 0) === 15.U, "h71".U(8.W), "hff".U(8.W)))))))))))))))))
  io.gpio.seg(1) := Mux(seg_reg(7, 4) === 0.U, "h03".U(8.W), Mux(seg_reg(7, 4) === 1.U, "h9f".U(8.W), Mux(seg_reg(7, 4) === 2.U, "h25".U(8.W), Mux(seg_reg(7, 4) === 3.U, "h0d".U(8.W), Mux(seg_reg(7, 4) === 4.U, "h99".U(8.W), Mux(seg_reg(7, 4) === 5.U, "h49".U(8.W), Mux(seg_reg(7, 4) === 6.U, "h41".U(8.W), Mux(seg_reg(7, 4) === 7.U, "h1f".U(8.W), Mux(seg_reg(7, 4) === 8.U, "h01".U(8.W), Mux(seg_reg(7, 4) === 9.U, "h09".U(8.W), Mux(seg_reg(7, 4) === 10.U, "h11".U(8.W), Mux(seg_reg(7, 4) === 11.U, "hc1".U(8.W), Mux(seg_reg(7, 4) === 12.U, "h63".U(8.W), Mux(seg_reg(7, 4) === 13.U, "h85".U(8.W), Mux(seg_reg(7, 4) === 14.U, "h61".U(8.W), Mux(seg_reg(7, 4) === 15.U, "h71".U(8.W), "hff".U(8.W)))))))))))))))))
  io.gpio.seg(2) := Mux(seg_reg(11, 8) === 0.U, "h03".U(8.W), Mux(seg_reg(11, 8) === 1.U, "h9f".U(8.W), Mux(seg_reg(11, 8) === 2.U, "h25".U(8.W), Mux(seg_reg(11, 8) === 3.U, "h0d".U(8.W), Mux(seg_reg(11, 8) === 4.U, "h99".U(8.W), Mux(seg_reg(11, 8) === 5.U, "h49".U(8.W), Mux(seg_reg(11, 8) === 6.U, "h41".U(8.W), Mux(seg_reg(11, 8) === 7.U, "h1f".U(8.W), Mux(seg_reg(11, 8) === 8.U, "h01".U(8.W), Mux(seg_reg(11, 8) === 9.U, "h09".U(8.W), Mux(seg_reg(11, 8) === 10.U, "h11".U(8.W), Mux(seg_reg(11, 8) === 11.U, "hc1".U(8.W), Mux(seg_reg(11, 8) === 12.U, "h63".U(8.W), Mux(seg_reg(11, 8) === 13.U, "h85".U(8.W), Mux(seg_reg(11, 8) === 14.U, "h61".U(8.W), Mux(seg_reg(11, 8) === 15.U, "h71".U(8.W), "hff".U(8.W)))))))))))))))))
  io.gpio.seg(3) := Mux(seg_reg(15, 12) === 0.U, "h03".U(8.W), Mux(seg_reg(15, 12) === 1.U, "h9f".U(8.W), Mux(seg_reg(15, 12) === 2.U, "h25".U(8.W), Mux(seg_reg(15, 12) === 3.U, "h0d".U(8.W), Mux(seg_reg(15, 12) === 4.U, "h99".U(8.W), Mux(seg_reg(15, 12) === 5.U, "h49".U(8.W), Mux(seg_reg(15, 12) === 6.U, "h41".U(8.W), Mux(seg_reg(15, 12) === 7.U, "h1f".U(8.W), Mux(seg_reg(15, 12) === 8.U, "h01".U(8.W), Mux(seg_reg(15, 12) === 9.U, "h09".U(8.W), Mux(seg_reg(15, 12) === 10.U, "h11".U(8.W), Mux(seg_reg(15, 12) === 11.U, "hc1".U(8.W), Mux(seg_reg(15, 12) === 12.U, "h63".U(8.W), Mux(seg_reg(15, 12) === 13.U, "h85".U(8.W), Mux(seg_reg(15, 12) === 14.U, "h61".U(8.W), Mux(seg_reg(15, 12) === 15.U, "h71".U(8.W), "hff".U(8.W)))))))))))))))))
  io.gpio.seg(4) := Mux(seg_reg(19, 16) === 0.U, "h03".U(8.W), Mux(seg_reg(19, 16) === 1.U, "h9f".U(8.W), Mux(seg_reg(19, 16) === 2.U, "h25".U(8.W), Mux(seg_reg(19, 16) === 3.U, "h0d".U(8.W), Mux(seg_reg(19, 16) === 4.U, "h99".U(8.W), Mux(seg_reg(19, 16) === 5.U, "h49".U(8.W), Mux(seg_reg(19, 16) === 6.U, "h41".U(8.W), Mux(seg_reg(19, 16) === 7.U, "h1f".U(8.W), Mux(seg_reg(19, 16) === 8.U, "h01".U(8.W), Mux(seg_reg(19, 16) === 9.U, "h09".U(8.W), Mux(seg_reg(19, 16) === 10.U, "h11".U(8.W), Mux(seg_reg(19, 16) === 11.U, "hc1".U(8.W), Mux(seg_reg(19, 16) === 12.U, "h63".U(8.W), Mux(seg_reg(19, 16) === 13.U, "h85".U(8.W), Mux(seg_reg(19, 16) === 14.U, "h61".U(8.W), Mux(seg_reg(19, 16) === 15.U, "h71".U(8.W), "hff".U(8.W)))))))))))))))))
  io.gpio.seg(5) := Mux(seg_reg(23, 20) === 0.U, "h03".U(8.W), Mux(seg_reg(23, 20) === 1.U, "h9f".U(8.W), Mux(seg_reg(23, 20) === 2.U, "h25".U(8.W), Mux(seg_reg(23, 20) === 3.U, "h0d".U(8.W), Mux(seg_reg(23, 20) === 4.U, "h99".U(8.W), Mux(seg_reg(23, 20) === 5.U, "h49".U(8.W), Mux(seg_reg(23, 20) === 6.U, "h41".U(8.W), Mux(seg_reg(23, 20) === 7.U, "h1f".U(8.W), Mux(seg_reg(23, 20) === 8.U, "h01".U(8.W), Mux(seg_reg(23, 20) === 9.U, "h09".U(8.W), Mux(seg_reg(23, 20) === 10.U, "h11".U(8.W), Mux(seg_reg(23, 20) === 11.U, "hc1".U(8.W), Mux(seg_reg(23, 20) === 12.U, "h63".U(8.W), Mux(seg_reg(23, 20) === 13.U, "h85".U(8.W), Mux(seg_reg(23, 20) === 14.U, "h61".U(8.W), Mux(seg_reg(23, 20) === 15.U, "h71".U(8.W), "hff".U(8.W)))))))))))))))))
  io.gpio.seg(6) := Mux(seg_reg(27, 24) === 0.U, "h03".U(8.W), Mux(seg_reg(27, 24) === 1.U, "h9f".U(8.W), Mux(seg_reg(27, 24) === 2.U, "h25".U(8.W), Mux(seg_reg(27, 24) === 3.U, "h0d".U(8.W), Mux(seg_reg(27, 24) === 4.U, "h99".U(8.W), Mux(seg_reg(27, 24) === 5.U, "h49".U(8.W), Mux(seg_reg(27, 24) === 6.U, "h41".U(8.W), Mux(seg_reg(27, 24) === 7.U, "h1f".U(8.W), Mux(seg_reg(27, 24) === 8.U, "h01".U(8.W), Mux(seg_reg(27, 24) === 9.U, "h09".U(8.W), Mux(seg_reg(27, 24) === 10.U, "h11".U(8.W), Mux(seg_reg(27, 24) === 11.U, "hc1".U(8.W), Mux(seg_reg(27, 24) === 12.U, "h63".U(8.W), Mux(seg_reg(27, 24) === 13.U, "h85".U(8.W), Mux(seg_reg(27, 24) === 14.U, "h61".U(8.W), Mux(seg_reg(27, 24) === 15.U, "h71".U(8.W), "hff".U(8.W)))))))))))))))))
  io.gpio.seg(7) := Mux(seg_reg(31, 28) === 0.U, "h03".U(8.W), Mux(seg_reg(31, 28) === 1.U, "h9f".U(8.W), Mux(seg_reg(31, 28) === 2.U, "h25".U(8.W), Mux(seg_reg(31, 28) === 3.U, "h0d".U(8.W), Mux(seg_reg(31, 28) === 4.U, "h99".U(8.W), Mux(seg_reg(31, 28) === 5.U, "h49".U(8.W), Mux(seg_reg(31, 28) === 6.U, "h41".U(8.W), Mux(seg_reg(31, 28) === 7.U, "h1f".U(8.W), Mux(seg_reg(31, 28) === 8.U, "h01".U(8.W), Mux(seg_reg(31, 28) === 9.U, "h09".U(8.W), Mux(seg_reg(31, 28) === 10.U, "h11".U(8.W), Mux(seg_reg(31, 28) === 11.U, "hc1".U(8.W), Mux(seg_reg(31, 28) === 12.U, "h63".U(8.W), Mux(seg_reg(31, 28) === 13.U, "h85".U(8.W), Mux(seg_reg(31, 28) === 14.U, "h61".U(8.W), Mux(seg_reg(31, 28) === 15.U, "h71".U(8.W), "hff".U(8.W)))))))))))))))))

  val pready = RegInit(false.B)
  io.in.pready := pready
  //val prdata = Mux(state === sAccess && io.in.pwrite === false.B && io.in.paddr === "h10002004".U, Cat(Fill(16, 0.U), switch_reg), 0.U )
  io.in.prdata := Cat(Fill(16, 0.U), switch_reg)
  io.in.pslverr := false.B
  val sIdle :: sSetup :: sAccess :: Nil = Enum(3)
  val state = RegInit(sIdle)
  switch(state) {
    is (sIdle) {
      pready := false.B
      state := Mux(io.in.psel === true.B, sSetup, sIdle) 
    }
    is (sSetup) {
      pready := false.B
      state := Mux(io.in.penable === true.B, sAccess, sSetup)
    }
    is (sAccess) {
      
      led_reg := Mux(io.in.pwrite === true.B && io.in.paddr === "h10002000".U(32.W), io.in.pwdata(15, 0),led_reg)
      seg_reg := Mux(io.in.pwrite === true.B && io.in.paddr === "h10002008".U(32.W), io.in.pwdata, seg_reg)
      pready := true.B
      state := Mux(io.in.psel === true.B && io.in.penable === false.B, sSetup, Mux(io.in.psel === false.B, sIdle, sAccess))
    }
  }



}

class APBGPIO(address: Seq[AddressSet])(implicit p: Parameters) extends LazyModule {
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
    val gpio_bundle = IO(new GPIOIO)

    val mgpio = Module(new gpioChisel)
    mgpio.io.clock := clock
    mgpio.io.reset := reset
    mgpio.io.in <> in
    gpio_bundle <> mgpio.io.gpio
  }
}
