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
folder name is given. Only those files with a full pathname matching the regex
will be included in the analysis.

## Examples

In the first example, we set an environment variable "LOGGING" and then run this on a single log file.
The file is read and results printed as a json string.
````$shell
$ export LOGGING=true
$ echo $LOGGING
true
$ java -jar lograte-0.1.0-SNAPSHOT-standalone.jar data/LogSources/u_ex17022801.log
Checking the LOGGING env var
2017-03-03 04:43:53 - Analyzing data/LogSources/u_ex17022801.log
{"count":59,"start":"2017-02-28T01:00:58.000Z","end":"2017-02-28T01:58:58.000Z","rate-per-second":0.016954023,"duration-in-seconds":3480}
$
````

When we run this with a folder name, all the files under that folder will
be analyzed, and the final summary result printed as a json string.

````$shell
$ java -jar lograte-0.1.0-SNAPSHOT-standalone.jar data/LogSources
Checking the LOGGING env var
2017-03-03 04:43:19 - Analyzing data/LogSources/u_ex17022801.log
2017-03-03 04:43:19 - 	messages: 59, seconds: 3480, rate: 0.016954
2017-03-03 04:43:19 - Analyzing data/LogSources/u_ex17022802.log
2017-03-03 04:43:19 - 	messages: 59, seconds: 3481, rate: 0.016949
2017-03-03 04:43:19 - Analyzing data/LogSources/u_ex17022803.log
2017-03-03 04:43:19 - 	messages: 59, seconds: 3480, rate: 0.016954
{"total-rate":0.05085719935595989,"highest-rate":0.016954023,"count":177,"files":3}
$
````
Note that the total rate reported is assuming worst case -- that these logs
are generated concurrently, so the rates are additive.

### Bugs

### Future enhancements

* Give a sampling size less than the whole file
* Output points representing 10, 20, ... 90 quartile rates on sample size
* Output points showing average rate per sample size across the file
* Determine what files are serially generated, and do not represent added concurrent throughput
* Create audio tones corresponding to the event rate to provide distinct audio signature



