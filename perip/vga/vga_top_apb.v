module vga_top_apb(
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

  output [7:0]  vga_r,
  output [7:0]  vga_g,
  output [7:0]  vga_b,
  output        vga_hsync,
  output        vga_vsync,
  output        vga_valid
);
reg [23:0] vga_input[524287:0];
reg [31:0] tmpaddr;
wire [9:0] h_addr;
wire [9:0] v_addr;
wire [9:0] x_addr;
wire [9:0] y_addr;
wire [18:0] realaddr;
reg [23:0] vga_data;
reg rdata;
reg is_sync;
integer i;
vga_ctrl vgact (clock, reset, vga_data, is_sync, h_addr, 
v_addr, vga_vsync, vga_hsync, vga_valid, 
vga_r, vga_g, vga_b, x_addr, y_addr);

assign in_pslverr = 1'b0;
assign in_pready  = in_penable && in_psel;
assign in_prdata = {32{rdata}};
assign realaddr = tmpaddr[18:0];
always @(posedge clock) begin
  if (reset) begin
    for (i = 0; i < 524288 ; i = i + 1) begin
      vga_input[i] <= 24'b0;
    end
    
    tmpaddr <= 0;
    vga_data <= 0;
    rdata <= 0;
    is_sync <= 0;

  end
end

always @(posedge clock) begin
  if (in_pready) begin
    if (in_pwrite == 1 && in_paddr == 32'h211ffffc) begin
      is_sync <= in_pwdata[0];
    end
    else if (in_pwrite == 1 && in_paddr != 32'h211ffffc) begin
      vga_input[realaddr+1] <= in_pwdata[23:0];
    end
  end
  else if (in_psel) begin
    if (in_pwrite == 0 && in_paddr == 32'h211ffffc) begin
      rdata <= is_sync;
    end
    else if (in_pwrite == 1 && in_paddr < 32'h211ffffc && in_paddr >= 32'h21000000) begin
      tmpaddr <= ((in_paddr - 32'h21000000)/4);
    end
  end
end

always @(posedge clock) begin
  if (h_addr == 800 && v_addr == 525) begin
    is_sync <= 0;
  end
end

always @(posedge clock) begin
  if (is_sync) begin
    vga_data <= vga_input[y_addr[8:0] * 640 + x_addr];
  end
end

endmodule
