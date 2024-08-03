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
reg [7:0] data1;
reg [7:0] data2;
reg [7:0] a_data;
reg overflow;
reg [7:0] k_count;
reg isput;
reg [7:0] rdata[63:0];
reg [7:0] rrdata;
reg [5:0]wi;
reg [5:0]ri;
reg [15:0]data_num;
//reg [3:0] rcount;
reg is_ok;
reg [3:0] ccount;
ps2keyboard ps2kb (
  .clk(clock),
  .ps2_clk(ps2_clk),
  .ps2_data(ps2_data),
  .rst(reset),
  .data(data),
  .a_data(a_data),
  .overflow(overflow),
  .k_count(k_count),
  .isput(isput),
  .count(ccount)
);


assign in_pslverr = 1'b0;
assign in_pready  = in_penable && in_psel && !in_pwrite && is_ok;

always @(posedge clock) begin
  if (reset) begin
    wi <= 0;
    ri <= 0;
    data_num <= 0;
    rrdata <= 0;
  end
end

always @(posedge clock) begin
   if (isput == 1 && ccount == 0) begin
       rdata[wi] <= data;
       wi <= wi + 1;
       data_num <= data_num + 1;    
  end
  
end

always @(posedge clock) begin
  if (in_pready) begin
    rrdata <= 0;
    is_ok <= 0;
    rdata[ri] <= 0;
    if (ri != wi) begin
      ri <= ri + 1;
      data_num <= data_num - 1;
    end

  end
  else if (in_psel)begin
    //rdata <= data;
    is_ok <= 1;
    rrdata <= rdata[ri];
  end
  else begin
    rrdata <= rrdata;
  end
end

assign in_prdata = {4{rrdata}};


endmodule
