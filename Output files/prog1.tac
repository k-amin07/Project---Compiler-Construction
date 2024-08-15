0  mov t0 "enter number"
1  out t0
2  in i
3  mov t1 0
4  if i GT t1 goto 6
5  goto 22
6  mov t2 0
7  mov first t2
8  mov t3 1
9  mov j t3
10  mov t4 i
11  if j LE t4 goto 13
12  goto 19
13  mov t5 j
14  out t5
15  mov t6 j
16  mov t7 1
17  add j t7
18  goto 11
19  mov t8 i
20  sub i ^ 1
21  goto 4
22  ret i
