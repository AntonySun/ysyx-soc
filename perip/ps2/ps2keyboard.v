module ps2keyboard(
input clk, ps2_clk,ps2_data, rst,
output reg [7:0] data,
output reg [7:0] a_data,
output reg [7:0] seg1,
output reg [7:0] seg2,
output reg [7:0] seg3,
output reg [7:0] seg4,
output reg [7:0] seg5,
output reg [7:0] seg6,
output reg overflow,
output reg [7:0] k_count,
output reg isput
);

wire [7:0] sdata;
reg nextdata_n;
reg ready;
wire sampling;
data_recieve dr(clk, ready, sdata, nextdata_n, data);
keyboard_control kc(clk, rst, ps2_clk, ps2_data,  sdata, ready, nextdata_n, overflow, k_count, isput);
keycode_ascii ka(data, a_data);

endmodule
