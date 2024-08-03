module data_recieve(
    input clk,
    input ready,
    input [7:0] r_data,
    output reg nextdata_n,
    output reg [7:0] o_data
);

always @(posedge clk) begin
    if (ready == 1'b1) begin      
        nextdata_n = 1'b1;
    end
 end

always @(posedge clk) begin
    if (ready == 1'b1 && nextdata_n == 1'b1)begin
      o_data = r_data;
      nextdata_n = 0;
    end
end

endmodule
