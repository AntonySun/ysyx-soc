module ps2_top_apb(
  input         clock,
  input         reset,
  input  [31:0] in_paddr,
  input         in_psel,
  input         in_penable,
  input  [2:0]  in_pprot,
  input         in_pwrite,
  input  [31:0] in_pwdata,
  input  [3:0]  in_pstrb,
  output        in_pready,
  output [31:0] in_prdata,
  output        in_pslverr,

  input         ps2_clk,
  input         ps2_data
);

reg [7:0] data;
reg [7:0] a_data;
reg overflow;
reg [7:0] k_count;
reg isput;

ps2keyboard ps2kb (
  .clk(clock),
  .ps2_clk(ps2_clk),
  .ps2_data(ps2_data),
  .rst(reset),
  .data(data),
  .a_data(a_data),
  .overflow(overflow),
  .k_count(k_count),
  .isput(isput)
);

assign in_pslverr = 1'b0;
assign in_pready  = in_penable && in_psel && !in_pwrite;
assign in_prdata = {4{data}};


endmodule
