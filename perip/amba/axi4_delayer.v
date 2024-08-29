module axi4_delayer(
  input         clock,
  input         reset,

  output        in_arready,
  input         in_arvalid,
  input  [3:0]  in_arid,
  input  [31:0] in_araddr,
  input  [7:0]  in_arlen,
  input  [2:0]  in_arsize,
  input  [1:0]  in_arburst,
  input         in_rready,
  output        in_rvalid,
  output [3:0]  in_rid,
  output [63:0] in_rdata,
  output [1:0]  in_rresp,
  output        in_rlast,
  output        in_awready,
  input         in_awvalid,
  input  [3:0]  in_awid,
  input  [31:0] in_awaddr,
  input  [7:0]  in_awlen,
  input  [2:0]  in_awsize,
  input  [1:0]  in_awburst,
  output        in_wready,
  input         in_wvalid,
  input  [63:0] in_wdata,
  input  [7:0]  in_wstrb,
  input         in_wlast,
                in_bready,
  output        in_bvalid,
  output [3:0]  in_bid,
  output [1:0]  in_bresp,

  input         out_arready,
  output        out_arvalid,
  output [3:0]  out_arid,
  output [31:0] out_araddr,
  output [7:0]  out_arlen,
  output [2:0]  out_arsize,
  output [1:0]  out_arburst,
  output        out_rready,
  input         out_rvalid,
  input  [3:0]  out_rid,
  input  [63:0] out_rdata,
  input  [1:0]  out_rresp,
  input         out_rlast,
  input         out_awready,
  output        out_awvalid,
  output [3:0]  out_awid,
  output [31:0] out_awaddr,
  output [7:0]  out_awlen,
  output [2:0]  out_awsize,
  output [1:0]  out_awburst,
  input         out_wready,
  output        out_wvalid,
  output [63:0] out_wdata,
  output [7:0]  out_wstrb,
  output        out_wlast,
                out_bready,
  input         out_bvalid,
  input  [3:0]  out_bid,
  input  [1:0]  out_bresp
);

  assign in_arready = out_arready;
  assign out_arvalid = in_arvalid;
  assign out_arid = in_arid;
  assign out_araddr = in_araddr;
  assign out_arlen = in_arlen;
  assign out_arsize = in_arsize;
  assign out_arburst = in_arburst;
  assign out_rready = (in_araddr >= 32'ha0000000 && in_araddr < 32'hc0000000) ? (okk1 == 1 && in_rready == 1) : in_rready;
  assign in_rvalid = (in_araddr >= 32'ha0000000 && in_araddr < 32'hc0000000) ? (okk1 == 1 && out_rvalid == 1) : out_rvalid;
  assign in_rid =  out_rid;
  assign in_rdata = out_rdata;
  assign in_rresp = out_rresp;
  assign in_rlast = out_rlast;
  assign in_awready = out_awready;
  assign out_awvalid = in_awvalid;
  assign out_awid = in_awid;
  assign out_awaddr = in_awaddr;
  assign out_awlen = in_awlen;
  assign out_awsize = in_awsize;
  assign out_awburst = in_awburst;
  assign in_wready = out_wready;
  assign out_wvalid = in_wvalid;
  assign out_wdata = in_wdata;
  assign out_wstrb = in_wstrb;
  assign out_wlast = in_wlast;
  assign out_bready = (in_awaddr >= 32'ha0000000 && in_awaddr < 32'hc0000000) ? (okk2 == 1 && in_bready == 1) : in_bready;
  assign in_bvalid = (in_awaddr >= 32'ha0000000 && in_awaddr < 32'hc0000000) ? (okk2 == 1 && out_bvalid == 1) : out_bvalid;
  assign in_bid = out_bid;
  assign in_bresp = out_bresp;

  
  reg [3:0] r_rid;
  reg [63:0] r_rdata;
  reg [1:0] r_rresp;
  reg r_rlast;

  reg [19:0] r_num;
  reg [15:0] r_counter;
  reg okk1;
  reg [2:0] burst_num;

  reg [19:0] w_num;
  reg [15:0] w_counter;
  reg w_done;
  reg okk2;

  always @(posedge clock) begin
    if (in_araddr >= 32'ha0000000 && in_araddr < 32'hc0000000) begin

      if (in_arvalid == 1) begin
        r_num <= 20'd128;
        burst_num <= 0;
        okk1 <= 0;
      end

      else if (r_counter == 0 && okk1 == 0 && out_rvalid == 1 ) begin
        okk1 <= 1;
      end

      else if (okk1 == 1 && in_rready == 1 && out_rvalid == 1 && out_rlast == 0) begin
        okk1 <= 0;
      end

    end

    else begin
      okk1 <= 1;
    end
    
  end

  always @(posedge clock) begin
    if (in_araddr >= 32'ha0000000 && in_araddr < 32'hc0000000) begin
      
      if (okk1 == 0 && out_rvalid == 0) begin

        r_num <= r_num + 20'd128;
        r_counter <= r_num[19:4];

      end

      else if (okk1 == 0 && out_rvalid == 1 && burst_num < 3'd4) begin

        if (r_counter > 0) begin
          if (r_counter == 1) begin
            burst_num <= burst_num + 1; 
          end
          r_counter <= r_counter - 1;
        end
        else if (r_counter == 0) begin
          r_counter <= r_num[19:4];
        end

      end
    end

    else begin

      r_counter <= 0;
      r_num <= 0;

    end

  end

  always @(posedge clock) begin
    if (in_awaddr >= 32'ha0000000 && in_awaddr < 32'hc0000000) begin
      if (in_awvalid == 1 || in_wvalid == 1) begin
        okk2 <= 0;
        w_num <= 20'd128;
      end
      else if (w_counter == 0 && okk2 == 0 && out_bvalid == 1) begin
        okk2 <= 1;
      end
      else if (okk2 == 1 && out_bvalid == 1 && in_bready == 1 && in_wlast == 0) begin
        okk2 <= 0;
      end
    end

    else begin
      okk2 <= 1;
    end

  end

  always @(posedge clock) begin
    if (in_awaddr >= 32'ha0000000 && in_awaddr < 32'hc0000000) begin
      if (okk2 == 0 && out_bvalid == 0) begin

        w_num <= w_num + 20'd128;
        w_counter <= w_num[19:4];

      end

      else if (okk2 == 0 && out_bvalid == 1) begin
        if (w_counter > 0) begin
          w_counter <= w_counter - 1;
        end
        else if (w_counter == 0) begin
          w_num <= 0;
        end
      end

    end

    else begin

      w_counter <= 0;
      w_num <= 0;

    end

  end
  always @(posedge clock) begin
    if (in_awvalid == 1 || in_wvalid == 1) begin
        w_done <= 0;
    end
    else if (out_bvalid == 1) begin
      w_done <= 1;
    end
    else if (okk2 == 1 && w_done == 1) begin
      w_done <= 0;
    end
  end

endmodule
