module apb_delayer(
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

  output [31:0] out_paddr,
  output        out_psel,
  output        out_penable,
  output [2:0]  out_pprot,
  output        out_pwrite,
  output [31:0] out_pwdata,
  output [3:0]  out_pstrb,
  input         out_pready,
  input  [31:0] out_prdata,
  input         out_pslverr
);
 
  reg [19:0] r_num;
  reg [15:0] r_counter;
  reg okk;
  reg [31:0] rdata;
 

  assign out_paddr   = in_paddr;
  assign out_psel    = okk ? in_psel : 0;
  assign out_penable = okk ? in_penable : 0;
  assign out_pprot   = in_pprot;
  assign out_pwrite  = in_pwrite;
  assign out_pwdata  = in_pwdata;
  assign out_pstrb   = in_pstrb;
  assign in_pready   = (in_paddr >= 32'ha0000000 && in_paddr < 32'hc0000000) ? (okk == 1 && r_counter == 1 && in_penable == 1) : out_pready;
  assign in_prdata   = (in_paddr >= 32'ha0000000 && in_paddr < 32'hc0000000) ? rdata : out_prdata;
  assign in_pslverr  = out_pslverr;

 

  always @(posedge clock) begin

    if (in_paddr >= 32'ha0000000 && in_paddr < 32'hc0000000  ) begin
      if (in_psel == 1 && out_pready == 0 && okk == 1)begin
        r_num <= r_num + 20'd128;
      r_counter <= r_num[19:4];
      end
      else if (okk == 0) begin
      if (r_counter > 1) begin
        r_counter <= r_counter - 1;  
      end
      else if (r_counter == 1)begin
        r_num <= 0;
      end
    end
    end
    
    else begin
      r_counter <= 1;
      r_num <= 0;
    end

  end

always @(posedge clock) begin
  if ( in_paddr >= 32'ha0000000 && in_paddr < 32'hc0000000) begin
    if ((out_pready == 1 && in_penable == 1 && in_psel == 1)) begin   
      okk <= 0;
      rdata <= out_prdata;
    end
    else if (r_counter == 1 && okk == 0)
      okk <= 1;
  end
  else begin
    okk <= 1;
  end
end

  
endmodule
