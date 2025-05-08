
### Create Dummy File (Java)

by: Keith Fenske, https://kwfenske.github.io/

CreateDummyFile is a Java 1.4 console application to create a file with a given
size, and to fill the file with a repeating pattern or pseudo-random data. Such
a "dummy" file can replace the space occupied by a regular file that is not
available.

Two parameters are required on the command line. The first parameter must be
the size of the file in bytes, as a decimal number, without commas or digit
grouping. Suffixes are recognized for kilobytes, megabytes, etc. The second
parameter is the output file name. You may need to quote the name.

Options select which data values to write for the bytes:

	-d# = one or more decimal bytes from 000 to 255
	-h# = one or more hexadecimal bytes from 00 to FF
	-o = write all ones, 0xFF bytes
	-p# = text pattern to repeat, in local character set
	-r = write pseudo-random data (default)
	-z = write all zeros, 0x00 bytes

These options are mutually exclusive; only one option should appear. Options go
before the parameters. An example to write a 512-byte random file is:

	java  CreateDummyFile2  512  x.dat

Another example to write a file with 32,768 bytes (32 KB) full of zeros is:

	java  CreateDummyFile2  -z  32k  x.dat

There is no graphical interface (GUI) for this program; it must be run from a
command prompt, command shell, or terminal window.

Download the ZIP file here: https://kwfenske.github.io/create-dummy-file-java.zip

Released under the terms and conditions of the Apache License (version 2.0 or
later) and/or the GNU General Public License (GPL, version 2 or later).
