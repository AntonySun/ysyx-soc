module keyboard_control(clk,clrn,ps2_clk,ps2_data,data,
                    ready,nextdata_n,overflow, key_count, isput, count
                    );
    input clk,clrn,ps2_clk,ps2_data;
    input nextdata_n;
    output [7:0] data;
    output reg ready;
    output reg overflow;     // fifo overflow
    output reg [7:0] key_count;
    output reg isput;
    output reg [3:0] count;
    //output sampling;
    // internal signal, for test
    reg [9:0] buffer;        // ps2_data bits
    reg [7:0] fifo[7:0];     // data fifo
    reg [2:0] w_ptr,r_ptr;   // fifo write and read pointers
      // count ps2_data bits
    // detect falling edge of ps2_clk
    reg [2:0] ps2_clk_sync;
     reg ww;
     reg [31:0]nkc;
    initial begin
       nkc = 0;
        key_count = 0;
        isput = 0;
        ww = 0;
    end

    always @(posedge clk) begin
        
        ps2_clk_sync <=  {ps2_clk_sync[1:0],ps2_clk};
    end

   wire sampling = ps2_clk_sync[2] & ~ps2_clk_sync[1];
   wire nsampling = ~sampling;

    always @(posedge clk) begin
        if (clrn == 1) begin // rese
            count <= 0; w_ptr <= 0; r_ptr <= 0; overflow <= 0; ready<= 0;
        end
        else begin
            if ( ready ) begin // read to output next data
                if(nextdata_n == 1'b0) //read next data
                begin
                    r_ptr <= r_ptr + 3'b1;
                    if(w_ptr==(r_ptr+1'b1)) //empty
                        ready <= 1'b0;
                    
                end
            end
            if (sampling) begin
              
              if (count == 4'd10) begin
                if ((buffer[0] == 0) &&  // start bit
                    (ps2_data)       &&  // stop bit
                    (^buffer[9:1])) begin // odd  parity
                    //isput <= 1;
                   // $display("receive %x", buffer[8:1]);
                    if (buffer[8:1] != 8'hf0 && buffer[8:1] != fifo[w_ptr - 1] && fifo[w_ptr - 1] != 8'hf0 && fifo[w_ptr - 1] != 8'he0)
                      key_count = key_count + 1;
                     else if(buffer[8:1]!= 8'hf0 && buffer[8:1] == fifo[w_ptr - 1] && fifo[w_ptr - 2] == 8'hf0)
                       key_count = key_count + 1;
                     else if (buffer[8:1] != 8'hf0 && buffer[8:1] == fifo[w_ptr - 2] && fifo[w_ptr - 1] == 8'he0)
                       key_count = key_count + 1;
                     else if (buffer[8:1] != 8'hf0 && buffer[8:1] == fifo[w_ptr - 2] && buffer[8:1] != 8'he0 && fifo[w_ptr - 1] == 8'he0 && fifo[w_ptr - 3] == 8'he0)
                       key_count = key_count;
                     else
                       key_count = key_count;
                    fifo[w_ptr] <= buffer[8:1];  // kbd scan code
                  //  ww = w_ptr;
                    w_ptr <= w_ptr+3'b1;
                    ready <= 1'b1;
                    overflow <= overflow | (r_ptr == (w_ptr + 3'b1));
                end
                //isput <= 0;
                count <= 0;     // for next
              end else begin
                buffer[count] <= ps2_data;  // store ps2_data
                count <= count + 3'b1;
              end
            end
            else if(sampling == 0)begin
            nkc = nkc + 1;
            //if (nkc %12000000 == 0)begin
            //isput <= 0; 
            //end
            end
        end
    end
  always @(posedge clk) begin
    if (count == 4'd10) begin
      isput <= 1;
    end
    else begin
      isput <= 0;
    end
  end
    assign data = fifo[r_ptr]; //always set output data

endmodule
