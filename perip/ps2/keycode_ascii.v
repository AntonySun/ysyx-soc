module keycode_ascii(
    input [7:0] keycode,
    output reg [7:0] a_code
);
reg [7:0] lookup_table [255:0];

initial begin
    lookup_table[8'h16] = 8'h31;
    lookup_table[8'h1e] = 8'h32;
    lookup_table[8'h26] = 8'h33;
    lookup_table[8'h25] = 8'h34;
    lookup_table[8'h2e] = 8'h35;
    lookup_table[8'h36] = 8'h36;
    lookup_table[8'h3d] = 8'h37;
    lookup_table[8'h3e] = 8'h38;
    lookup_table[8'h46] = 8'h39;
    lookup_table[8'h45] = 8'h30;
    lookup_table[8'h15] = 8'h71;
    lookup_table[8'h1d] = 8'h77;
    lookup_table[8'h24] = 8'h45;
    lookup_table[8'h2d] = 8'h72;
    lookup_table[8'h2c] = 8'h54;
    lookup_table[8'h35] = 8'h79;
    lookup_table[8'h3c] = 8'h75;
    lookup_table[8'h43] = 8'h69;
    lookup_table[8'h44] = 8'h4f;
    lookup_table[8'h4d] = 8'h50;
    lookup_table[8'h1c] = 8'h41;
    lookup_table[8'h1b] = 8'h53;
    lookup_table[8'h23] = 8'h44;
    lookup_table[8'h2B] = 8'h46;
    lookup_table[8'h34] = 8'h47;
    lookup_table[8'h33] = 8'h48;
    lookup_table[8'h3b] = 8'h4a;
    lookup_table[8'h42] = 8'h4b;
    lookup_table[8'h4b] = 8'h4c;
    lookup_table[8'h1a] = 8'h7a;
    lookup_table[8'h22] = 8'h78;
    lookup_table[8'h21] = 8'h43;
    lookup_table[8'h2a] = 8'h75;
    lookup_table[8'h32] = 8'h42;
    lookup_table[8'h31] = 8'h4e;
    lookup_table[8'h3a] = 8'h4d;
    lookup_table[8'h54] = 8'h5b;
    lookup_table[8'h5b] = 8'h5d;
    lookup_table[8'h4c] = 8'h3b;
    lookup_table[8'h52] = 8'h27;
    lookup_table[8'h41] = 8'h2c;
    lookup_table[8'h41] = 8'h2e;
    lookup_table[8'h4a] = 8'h2f;
    lookup_table[8'h5d] = 8'h5c;
    lookup_table[8'h4e] = 8'h5a;
    lookup_table[8'h55] = 8'h3d;
    lookup_table[8'h0e] = 8'h60;
    lookup_table[8'h5a] = 8'h0d;
    lookup_table[8'h0d] = 8'h09;
end

always@(*)begin
    a_code = lookup_table[keycode];
end

endmodule