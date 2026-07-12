Alexander Lap: 633426257

Section 1:
To run the program, just run Main directly or have the run config set to Main through Application

Section 2:
All parts of the program are working and have been tested to project specification
Please ensure all RandomV values are not padded in any and proper spacing is used.
Below is the format for every command used in my program:

CREATE INDEX ON Project2Dataset (RandomV)

SELECT * FROM Project2Dataset WHERE RandomV = 403

SELECT * FROM Project2Dataset WHERE RandomV > 5 AND RandomV < 4037

SELECT * FROM Project2Dataset WHERE RandomV != 55

Section 3:
I created a `combineSearches` routine that builds a hash table mapping each file ID to a linked list of record offsets.
By doing so, every search operation only needs to read each file once—instead of fetching records one at a time—effectively
implementing a sparse index. I also introduced a `RecordLocation` type to bundle together the file number and byte‐offset
for each record sharing the same `RandomV` key, which greatly simplifies record lookup and management.