ajlap: 633426257

Section 1:
To compile and execute the code, all you need is to run 'Main.java'. You can set configuration in the top right
near the run button to 'Application' and set file to 'Main' which is the same thing. All that's left is to run
the file and input the command listed in Section 2.

Section 2:
All components of the program have been implemented and tested in accordance with the project specifications.
The only deviation occurs in Section 3: Building Block-Level Nested-Loop Join. Due to the significant runtime
of this query, I included console output to indicate when processing of each file from Dataset A is complete.
This feedback serves to reassure the user that the query is still in progress. During testing, this join
produced 50,717,894 qualifying results and required approximately 596,673 ms to complete on my machine.
Although similar progress output is not included for Sections 2 and 4, these sections also involve relatively
long runtimes.

Below are all the command variants used to test the program:

Below are all variants of the commands needed to test the program:

SELECT A.Col1, A.Col2, B.Col1, B.Col2  FROM A, B WHERE A.RandomV = B.RandomV

SELECT count(*) FROM A, B WHERE A.RandomV > B.RandomV

SELECT Col2, SUM(RandomV) FROM A GROUP BY Col2

SELECT Col2, SUM(RandomV) FROM B GROUP BY Col2

SELECT Col2, AVG(RandomV) FROM A GROUP BY Col2

SELECT Col2, AVG(RandomV) FROM B GROUP BY Col2

Section 3:
The hash table is organized by dataset, with records within each dataset sorted first by file number and then by record number.
This structure enabled me to compute index boundaries between datasets, which in turn allowed for efficient calculation of all
possible joins for a given index in the hash table. As a result, generating all valid join combinations became significantly more
streamlined. While the project does not optimize I/O—each required record incurs a disk access—this level of optimization was not
necessary according to the project specifications. The implementation satisfies all requirements outlined in the project guidelines.

