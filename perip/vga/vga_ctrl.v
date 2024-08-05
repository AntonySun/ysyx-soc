module vga_ctrl(
    input clk,
    input reset,
    input [23:0] vga_data,
    input is_sync,   
    output [9:0] h_addr,
    output [9:0] v_addr,
    output vsync,
    output hsync,
    output Vgavalid,
    output [7:0] vga_r,
    output [7:0] vga_g,
    output [7:0] vga_b,
    output [9:0] x_addr,
    output [9:0] y_addr

);

parameter hsynctime = 96;
parameter hactive = 144;
parameter hdataready = 784;
parameter hnext = 800;

parameter vsynctime = 2;
parameter vactive = 35;
parameter vdataready = 515;
parameter vnext = 525;

reg [9:0] x_cnt;
reg [9:0] y_cnt;
wire xvalid;
wire yvalid;

always @(posedge clk) begin
    if (reset) begin
        x_cnt <= 10'b1;
        y_cnt <= 10'b1;
    end
    else if (is_sync) begin
        if (x_cnt < hnext) begin
            x_cnt <= x_cnt + 1;
        end
        else if (x_cnt == hnext) begin
            x_cnt <= 1;
            if (y_cnt < vnext) begin
                y_cnt <= y_cnt + 1; 
            end
            else if (y_cnt == vnext) begin
                y_cnt <= 1;
            end
        end
    end
end


assign hsync = x_cnt > hsynctime;
assign vsync = y_cnt > vsynctime;
assign xvalid = (x_cnt > hactive) & (x_cnt <= hdataready);
assign yvalid = (y_cnt > vactive) & (y_cnt <= vdataready);

assign Vgavalid = xvalid & yvalid;
assign h_addr = x_cnt;
assign v_addr = y_cnt;
assign x_addr = xvalid ? (x_cnt - 10'd145) : 10'd0;
assign y_addr = yvalid ? (y_cnt - 10'd36) : 10'd0;
assign {vga_r, vga_g, vga_b} = vga_data;

endmodule