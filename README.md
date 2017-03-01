# lograte

Reads log files and tells you the average event rate per second.

## Installation

Download the jar from GitHub. You must have the java8 JRE installed to run this.

## Usage

Run the jar, specifying the name of the log file and options.

    $ java -jar lograte-0.1.0-standalone.jar [args]

Specify a log file full pathname to read that file. If the argument begins with ``@``, then open this file and read the file names, one per line.
If you specify the `-e` option, omit the file argument, since `-e` indicates you want to read from stdin. You can then pipe the content to it.

## Options

* `-e` to read the log contents from stdin. Omit the log file path argument.

### Bugs

### Future enhancements

* Give a sampling size less than the whole file
* Output points representing 10, 20, ... 90 quartile rates on sample size
* Output points showing average rate per sample size across the file
* Create audio tones corresponding to the event rate to provide distinct audio signature



