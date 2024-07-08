package ysyx

import chisel3._
import chisel3.util._

class bitrev extends BlackBox {
  val io = IO(Flipped(new SPIIO(1)))
}

class bitrevChisel extends RawModule { // we do not need clock and reset
  
  val io = IO(Flipped(new SPIIO(1)))
  
  val reset = Wire(Bool())
  reset := 0.U
  val bit_shift = withClockAndReset(io.sck.asClock, reset) {RegInit(0.U(8.W))}
  val count = withClockAndReset(io.sck.asClock, reset) {RegInit(0.U(4.W))}
  
  when (io.ss === 0.U) {
    
    count := count + 1.U
    when (count < 8.U) {
      bit_shift := (bit_shift >> 1) | Cat(io.mosi.asUInt, Fill(7, 0.U))
      io.miso := true.B
    }.otherwise {
      io.miso := bit_shift(7)
      bit_shift := bit_shift << 1.U
    }
     
  
  }.otherwise {
    io.miso := true.B
    count := 0.U
    bit_shift := 0.U
    
  
  }
  



  
}
