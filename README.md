# lograte

Reads log files and reports message rates observed.

## Installation

Download the jar from GitHub. You must have the java8 JRE installed to run this.

## Usage

Run the jar, specifying the name of the log file and options.

    $ java -jar lograte-0.1.0-SNAPSHOT-standalone.jar <path> [<regex>]

where
- \<path\> is a pathname to a file or folder
- \<regex\> is an optional regex filter for the full pathnames of files under the \<path\> folder

To analyze the log rate of a single log file, specify just the pathname to that
file as the only argument. If you pass a path to a folder, every file found
in a recursive walk into that folder will be analyzed, and the results
summarized. The optional regex is used to filter the file names when a
folder name is given. Only those non-zip files with a full pathname matching the regex
will be included in the analysis.

## Examples

In the first example, we set an environment variable "LOGGING" and then run this on a single log file.
The file is read and results printed as a json string.
````$shell
$ export LOGGING=true
$ echo $LOGGING
true
$ java -jar lograte-0.1.0-SNAPSHOT-standalone.jar /tmp/LogSources/u_ex17030112.log
Checking the LOGGING env var
2017-03-03 13:45:37 - Analyzing /tmp/LogSources/u_ex17030112.log
{"message-count":475054,"start":"2017-03-01T12:00:00.000Z","end":"2017-03-01T12:59:59.000Z","rate-per-second":131.99611,"duration-in-seconds":3599}
$
````

When we run this with a folder name, all the files under that folder will
be analyzed, and the final summary result printed as a json string.

````$shell
$ java -jar lograte-0.1.0-SNAPSHOT-standalone.jar /tmp/LogSources
Checking the LOGGING env var
2017-03-03 13:47:07 - Analyzing /tmp/LogSources/u_ex17030112.log
2017-03-03 13:47:08 - 	messages: 475054, seconds: 3599, rate: 131.996109
2017-03-03 13:47:08 - Analyzing /tmp/LogSources/u_ex17030113.log
2017-03-03 13:47:10 - 	messages: 601355, seconds: 3599, rate: 167.089462
2017-03-03 13:47:10 - Analyzing /tmp/LogSources/u_ex17030114.log
2017-03-03 13:47:12 - 	messages: 587527, seconds: 3599, rate: 163.247284
{"average-rate":154.11096,"combined-duration":10797,"highest-rate":167.08946,"message-count":1663936,"file-count":3}
$
````
Note that this application does not attempt to correlate timestamps
across files, so it can't tell you what the peak rate is at any given
time.

### Bugs

### Future enhancements

* Give a sampling size less than the whole file
* Output points representing 10, 20, ... 90 quartile rates on sample size
* Output a graph showing rates over any given file.
* Correlate times across files, to create an aggregate profile graph over time.
* Determine what files are serially generated, and do not represent added concurrent throughput
* Create audio tones corresponding to the event rate to provide distinct audio signature



