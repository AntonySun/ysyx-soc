module ps2keyboard(
input clk, ps2_clk,ps2_data, rst,
output reg [7:0] data,
output reg [7:0] a_data,
output reg overflow,
output reg [7:0] k_count,
output reg isput,
output reg [3:0] count
);

wire [7:0] sdata;
reg nextdata_n;
reg ready;
wire sampling;
data_recieve dr(clk, ready, sdata, nextdata_n, data);
keyboard_control kc(clk, rst, ps2_clk, ps2_data,  sdata, ready, nextdata_n, overflow, k_count, isput, count);
keycode_ascii ka(data, a_data);

endmodule
