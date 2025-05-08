/*
  Create Dummy File #2 - Create File With Random Contents Given Size
  Written by: Keith Fenske, http://kwfenske.github.io/
  Friday, 5 July 2024
  Java class name: CreateDummyFile2
  Copyright (c) 2024 by Keith Fenske.  Apache License or GNU GPL.

  This is a Java 1.4 console application to create a file with a given size,
  and to fill the file with a repeating pattern or pseudo-random data.  Such a
  "dummy" file can replace the space occupied by a regular file that is not
  available.

  Two parameters are required on the command line.  The first parameter must be
  the size of the file in bytes, as a decimal number, without commas or digit
  grouping.  Suffixes are recognized for kilobytes, megabytes, etc.  The second
  parameter is the output file name.  You may need to quote the name.

  Options select which data values to write for the bytes:

    -d# = one or more decimal bytes from 000 to 255
    -h# = one or more hexadecimal bytes from 00 to FF
    -o = write all ones, 0xFF bytes
    -p# = text pattern to repeat, in local character set
    -r = write pseudo-random data (default)
    -z = write all zeros, 0x00 bytes

  These options are mutually exclusive; only one option should appear.  Options
  go before the parameters.  An example to write a 512-byte random file is:

    java  CreateDummyFile2  512  x.dat

  Another example to write a file with 32,768 bytes (32 KB) full of zeros is:

    java  CreateDummyFile2  -z  32k  x.dat

  There is no graphical interface (GUI) for this program; it must be run from a
  command prompt, command shell, or terminal window.

  Apache License or GNU General Public License
  --------------------------------------------
  CreateDummyFile2 is free software and has been released under the terms and
  conditions of the Apache License (version 2.0 or later) and/or the GNU
  General Public License (GPL, version 2 or later).  This program is
  distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE.  See the license(s) for more details.  You should have
  received a copy of the licenses along with this program.  If not, see the
  http://www.apache.org/licenses/ and http://www.gnu.org/licenses/ web pages.
*/

import java.io.*;                 // standard I/O
import java.text.*;               // number formatting
import java.util.regex.*;         // regular expressions

public class CreateDummyFile2
{
  /* constants */

  static final int BUFFER_SIZE = 0x40000;
                                  // output buffer size in bytes (256 KB)
  static final int BYTE_MASK = 0x000000FF; // gets low-order byte from integer
  static final String COPYRIGHT_NOTICE =
    "Copyright (c) 2024 by Keith Fenske.  Apache License or GNU GPL.";
  static final int EXIT_FAILURE = -1; // incorrect request or errors found
  static final int EXIT_SUCCESS = 1; // request completed successfully
  static final int EXIT_UNKNOWN = 0; // don't know or nothing really done
  static final String PROGRAM_TITLE =
    "Create File With Random Contents Given Size - by: Keith Fenske";

  /* class variables */

  static NumberFormat formatComma; // formats with commas (digit grouping)
  static boolean mswinFlag;       // true if running on Microsoft Windows

/*
  main() method

  We run as a console application.  There is no graphical interface.
*/
  public static void main(String[] args)
  {
    byte[] buffer;                // byte buffer for writing output file
    long bytesDone;               // number of bytes written so far
    byte[] dataBytes;             // non-empty byte sequence, if not random
    int dataLength;               // number of bytes in <dataBytes>
    String fileName;              // name of file to be created
    long fileSize;                // total number of bytes to be written
    int i, k;                     // index variables
    FileOutputStream out;         // byte output stream for writing file
    boolean randomFlag;           // true if we write pseudo-random data
    java.util.Random randomGen;   // fancy pseudo-random number generator
    int thisSize;                 // number of bytes from current buffer
    String word;                  // one parameter from command line

    /* Initialize variables used by both console and GUI applications. */

    dataBytes = null;             // ignored when <randomFlag> is true
    fileName = null;              // by default, there is no file name
    fileSize = -1;                // by default, there is no file size
    formatComma = NumberFormat.getInstance(); // current locale
    formatComma.setGroupingUsed(true); // use commas or digit groups
    mswinFlag = System.getProperty("os.name").startsWith("Windows");
    randomFlag = true;            // by default, write pseudo-random data
    randomGen = null;             // only used if <randomFlag> is true

    /* Check command-line parameters for options. */

    for (i = 0; i < args.length; i ++)
    {
      word = args[i].toLowerCase(); // easier to process if consistent case
      if (word.length() == 0)
      {
        /* Ignore empty parameters, which are more common than you might think,
        when programs are being run from inside scripts (command files). */
      }

      else if (word.equals("?") || word.equals("-?") || word.equals("/?")
        || word.equals("-h") || (mswinFlag && word.equals("/h"))
        || word.equals("-help") || (mswinFlag && word.equals("/help")))
      {
        showHelp();               // show help summary
        System.exit(EXIT_UNKNOWN); // exit application after printing help
      }

      else if (word.startsWith("-d") || (mswinFlag && word.startsWith("/d")))
      {
        /* One or more decimal values for data bytes.  Separators are optional
        but recommended.  Otherwise, every third digit starts a new byte. */

        dataBytes = parseDecBytes(word.substring(2));
        if ((dataBytes == null) || (dataBytes.length == 0))
        {
          System.err.println("Decimal byte data must be from 000 to 255: "
            + args[i]);
          showHelp();
          System.exit(EXIT_FAILURE);
        }
        randomFlag = false;       // use given value, not pseudo-random
      }

      else if (word.startsWith("-h") || (mswinFlag && word.startsWith("/h")))
      {
        /* One or more hexadecimal values for data bytes, and separators. */

        dataBytes = parseHexBytes(word.substring(2));
        if ((dataBytes == null) || (dataBytes.length == 0))
        {
          System.err.println("Hexadecimal byte data must be from 00 to FF: "
            + args[i]);
          showHelp();
          System.exit(EXIT_FAILURE);
        }
        randomFlag = false;
      }

      else if (word.equals("-o") || (mswinFlag && word.equals("/o")))
      {
        /* Write all ones: 0xFF bytes. */

        dataBytes = new byte[] {(byte) BYTE_MASK};
        randomFlag = false;
      }

      else if (word.startsWith("-p") || (mswinFlag && word.startsWith("/p")))
      {
        /* A text pattern to repeat.  Change code here to customize how the
        user's text is converted into bytes. */

        dataBytes = args[i].substring(2).getBytes(); // local character set
        if ((dataBytes == null) || (dataBytes.length == 0))
        {
          System.err.println(
            "Text pattern to repeat must have at least one byte: " + args[i]);
          showHelp();
          System.exit(EXIT_FAILURE);
        }
        randomFlag = false;
      }

      else if (word.startsWith("-r") || (mswinFlag && word.startsWith("/r")))
      {
        /* Write random data.  Undocumented option: if two or more hex bytes
        are given, then randomly select from those bytes.  A devious choice:

          -r30,30,30,30,30,31,31,31,31,32,32,32,33,33,33,34,34,35,35,36,36,37,37,38,39

        produces US-ASCII (UTF-8) digits that look like they have meaning, but
        don't.  This should trouble some file-recovery programs. */

        dataBytes = parseHexBytes(word.substring(2));
        if (dataBytes == null)    // some sort of parsing error
        {
          System.err.println("Random byte data must be hex from 00 to FF: "
            + args[i]);
          showHelp();
          System.exit(EXIT_FAILURE);
        }
        else if (dataBytes.length == 0) // most common case: nothing given
        {
          dataBytes = null;       // remove array data
          randomFlag = true;
        }
        else if (dataBytes.length == 1)
          randomFlag = false;     // not random if selecting from one item
        else
          randomFlag = true;      // game starts with two or more players
      }

      else if (word.equals("-z") || (mswinFlag && word.equals("/z")))
      {
        /* Write all zeros: 0x00 bytes. */

        dataBytes = new byte[] {(byte) 0};
        randomFlag = false;
      }

      else if (word.startsWith("-") || (mswinFlag && word.startsWith("/")))
      {
        System.err.println("Option not recognized: " + args[i]);
        showHelp();
        System.exit(EXIT_FAILURE);
      }

      else if (fileSize < 0)      // do we have a file size yet?
      {
        fileSize = parseFileSize(word); // returns file size or negative
        if (fileSize < 0)
        {
          System.err.println("First parameter must be file size in bytes: "
            + args[i]);
          showHelp();
          System.exit(EXIT_FAILURE);
        }
      }

      else if (fileName == null)  // do we have a file name yet?
      {
        fileName = args[i];       // accept any non-empty string (lazy)
      }

      else
      {
        System.err.println("Too many parameters on command line: " + args[i]);
        showHelp();
        System.exit(EXIT_FAILURE);
      }
    }

    /* Check for required parameters. */

    if (fileSize < 0)
    {
      System.err.println("Missing first parameter: file size in bytes.");
      showHelp();
      System.exit(EXIT_FAILURE);
    }

    if (fileName == null)
    {
      System.err.println("Missing second parameter: output file name.");
      showHelp();
      System.exit(EXIT_FAILURE);
    }

    /* Protect code below if the code above didn't set options properly. */

    if ((dataBytes == null) || (dataBytes.length == 0))
    {
      dataBytes = null;           // set or reset
      dataLength = 0;
      randomFlag = true;
    }
    else
      dataLength = dataBytes.length; // this number will be positive (>0)

    /* BufferedOutputStream is relatively slow for large files, so write our
    own big buffer directly with FileOutputStream, reducing the last piece to
    fit the desired final size.  The end of our buffer may not align with the
    end of a repeating pattern, unless the length of the pattern divides the
    size of our buffer without a remainder. */

    if (randomFlag)               // are we writing pseudo-random data?
    {
      buffer = new byte[BUFFER_SIZE]; // exact size, refill each time
      randomGen = new java.util.Random(); // create random number generator
    }
    else                          // no, fill buffer with constant data
    {
      buffer = new byte[BUFFER_SIZE + dataLength]; // extra copy of pattern
      for (i = 0; i < buffer.length; i ++)
        buffer[i] = dataBytes[i % dataLength];
    }

    /* We use the same buffer, over and over again, until we reach the user's
    desired size, or have an I/O error.  Since this is a console application,
    there is no "cancel" button or any way of interrupting the program, other
    than terminating the Java process. */

    try                           // catch file I/O errors
    {
      out = new FileOutputStream(fileName); // try to create output file
      bytesDone = 0;              // no bytes written so far
      k = 0;                      // starting offset in our buffer
      while (bytesDone < fileSize) // go until full size or I/O error
      {
        if (randomFlag)           // random bytes or random selection?
        {
          if (dataBytes == null)  // fill with pseudo-random data bytes
            randomGen.nextBytes(buffer);
          else                    // random selection from user's bytes
            for (i = 0; i < BUFFER_SIZE; i ++)
              buffer[i] = dataBytes[randomGen.nextInt(dataLength)];
        }
        else                      // adjust offset to maintain pattern
          k = (int) (bytesDone % dataLength);
        thisSize = (int) Math.min((fileSize - bytesDone), BUFFER_SIZE);
        out.write(buffer, k, thisSize); // write up to one block of data
        bytesDone += thisSize;    // more bytes done, closer to the end
      }
      out.close();                // try to close output file
      System.out.println("Created file with " + formatComma.format(bytesDone)
        + " bytes.");
    }
    catch (IOException ioe)
    {
      System.err.println("Error while writing file: " + ioe.getMessage());
      System.exit(EXIT_FAILURE);
    }

    System.exit(EXIT_SUCCESS);    // if we get here, everything is good

  } // end of main() method


/*
  parseDecBytes() method

  Convert decimal digits representing data bytes to real binary bytes.  Accept
  almost any US-ASCII punctuation as a separator.  Return <null> if the input
  has errors.
*/
  static byte[] parseDecBytes(String input)
  {
    char ch;                      // one character from input string
    int digitCount;               // number of digits found in current byte
    int i;                        // index variable
    int inputLength;              // number of characters in <input>
    byte[] result;                // cleaned up result with correct length
    byte[] tempBytes;             // encoded (binary) data bytes
    int tempCount;                // number of data bytes in <tempBytes>
    int value;                    // one binary data byte as an integer

    digitCount = 0;               // no digits found in current byte
    inputLength = input.length(); // number of digits or spaces, etc
    tempBytes = new byte[inputLength]; // always more than what we need
    tempCount = 0;                // no binary data bytes found
    value = 0;                    // no initial value for this data byte
    for (i = 0; i < inputLength; i ++)
    {
      ch = input.charAt(i);       // get one digit, punctuation, other
      if ((ch >= '0') && (ch <= '9')) // decimal digit?
      {
        value = (value * 10) + (ch - '0'); // shift old left, add new digit
        digitCount ++;            // at least one digit found
      }
      else if (((ch >= 0x00) && (ch <= 0x2F)) // ASCII Unicode punctuation
        || ((ch >= 0x3A) && (ch <= 0x40))
        || ((ch >= 0x5B) && (ch <= 0x60))
        || ((ch >= 0x7B) && (ch <= 0x7F))) // safe up to 0xBF
      {
        if (digitCount > 0) digitCount = 3; // accept one or two digits
      }
      else                        // draw the line at obviously bad input
        return(null);             // don't bother doing anything more

      if (digitCount >= 3)        // new binary byte every three digits
      {
        if (value > BYTE_MASK)    // overflow (from 256 to 999)
          return(null);
        tempBytes[tempCount ++] = (byte) value; // save one data byte
        digitCount = value = 0;   // no partial data for next digit
      }
    }
    if (digitCount > 0)           // could be trailing one or two digits
    {
      tempBytes[tempCount ++] = (byte) value; // can't overflow (0 to 99)
    }

    result = new byte[tempCount]; // truncate array to correct length
    for (i = 0; i < tempCount; i ++)
      result[i] = tempBytes[i];
    return(result);               // give caller correct byte array

  } // end of parseDecBytes() method


/*
  parseFileSize() method

  Given a string with a file size and suffix (KB, MB, GB, TB, etc), return the
  size in bytes as a non-negative long integer.  Return -1 if the value is too
  large or the string has poor syntax.  The size must be an integer (zero or
  more) without commas or other digit grouping, followed by an optional suffix.
  If no suffix is given, bytes are assumed.

  If this method is called repeatedly, the compiled regular expression should
  be saved between calls, and the Pattern.CASE_INSENSITIVE option can be used
  instead of String.toLowerCase().
*/
  static long parseFileSize(String input)
  {
    Matcher matcher;              // pattern matcher for given string
    long number;                  // integer part before suffix
    Pattern pattern;              // compiled regular expression
    char prefix;                  // first character of suffix (if any)
    long result;                  // our result (the file size)
    long scale;                   // scale factor for KB, MB, GB, etc
    String suffix;                // suffix string (may be null)

    result = -1;                  // assume file size is invalid
    pattern = Pattern.compile(    // compile our regular expression
      "\\s*(\\d+)\\s*(|b|k|kb|kib|m|mb|mib|g|gb|gib|t|tb|tib|p|pb|pib|e|eb|eib)\\s*");
    matcher = pattern.matcher(input.toLowerCase()); // parse given string
    if (matcher.matches())        // if string has proper syntax
    {
      /* Parse integer number before any suffix. */

      try                         // try to parse digits as unsigned integer
      {
        number = Long.parseLong(matcher.group(1));
      }
      catch (NumberFormatException nfe) // if not a number or bad syntax
      {
        number = -1;              // set result to an illegal value
      }

      /* Convert suffix string (if any) into a scale factor. */

      if (((suffix = matcher.group(2)) == null) || (suffix.length() == 0)
        || ((prefix = suffix.charAt(0)) == 'b'))
      {
        scale = 1;                // size is in bytes, no scaling required
      }
      else if (prefix == 'k') scale = 1L << 10; // kilobytes
      else if (prefix == 'm') scale = 1L << 20; // megabytes
      else if (prefix == 'g') scale = 1L << 30; // gigabytes
      else if (prefix == 't') scale = 1L << 40; // terabytes
      else if (prefix == 'p') scale = 1L << 50; // petabytes
      else if (prefix == 'e') scale = 1L << 60; // exabytes
      else                        // uknown suffix (poor regular expression)
      {
        number = -1;              // set result to an illegal value
        scale = 1;                // force scale factor to bytes
      }

      /* Multiply the two together if the result is within range. */

      if ((number >= 0) && (number <= (Long.MAX_VALUE / scale)))
        result = number * scale;
    }

    return(result);               // return our result (the file size)

  } // end of parseFileSize() method


/*
  parseHexBytes() method

  Convert hexadecimal characters representing data bytes to real binary bytes.
  Accept almost any US-ASCII punctuation as a separator.  Return <null> if the
  input has errors.
*/
  static byte[] parseHexBytes(String input)
  {
    char ch;                      // one character from input string
    int digitCount;               // number of digits found in current byte
    int i;                        // index variable
    int inputLength;              // number of characters in <input>
    byte[] result;                // cleaned up result with correct length
    byte[] tempBytes;             // encoded (binary) data bytes
    int tempCount;                // number of data bytes in <tempBytes>
    int value;                    // one binary data byte as an integer

    digitCount = 0;               // no digits found in current byte
    inputLength = input.length(); // number of hex digits or spaces, etc
    tempBytes = new byte[inputLength]; // always more than what we need
    tempCount = 0;                // no binary data bytes found
    value = 0;                    // no initial value for this data byte
    for (i = 0; i < inputLength; i ++)
    {
      ch = input.charAt(i);       // get one hex digit, punctuation, other
      if ((ch >= '0') && (ch <= '9')) // decimal digit?
      {
        value = (value << 4) + (ch - '0'); // shift old left, add new digit
        digitCount ++;            // at least one digit found
      }
      else if ((ch >= 'A') && (ch <= 'F')) // uppercase hex digit?
      {
        value = (value << 4) + (ch - 'A' + 10);
        digitCount ++;
      }
      else if ((ch >= 'a') && (ch <= 'f')) // lowercase hex digit?
      {
        value = (value << 4) + (ch - 'a' + 10);
        digitCount ++;
      }
      else if (((ch >= 0x00) && (ch <= 0x2F)) // ASCII Unicode punctuation
        || ((ch >= 0x3A) && (ch <= 0x40))
        || ((ch >= 0x5B) && (ch <= 0x60))
        || ((ch >= 0x7B) && (ch <= 0x7F))) // safe up to 0xBF
      {
        if (digitCount > 0) digitCount = 2; // accept a single digit
      }
      else                        // draw the line at obviously bad input
        return(null);             // don't bother doing anything more

      if (digitCount >= 2)        // new binary byte every two digits
      {
        tempBytes[tempCount ++] = (byte) value; // save one data byte
        digitCount = value = 0;   // no partial data for next digit
      }
    }
    if (digitCount > 0)           // could be a trailing single digit
    {
      tempBytes[tempCount ++] = (byte) value;
    }

    result = new byte[tempCount]; // truncate array to correct length
    for (i = 0; i < tempCount; i ++)
      result[i] = tempBytes[i];
    return(result);               // give caller correct byte array

  } // end of parseHexBytes() method


/*
  showHelp() method

  Show the help summary.  This is a UNIX standard and is expected for all
  console applications, even very simple ones.
*/
  static void showHelp()
  {
    System.err.println();
    System.err.println(PROGRAM_TITLE);
    System.err.println();
    System.err.println("This is a Java console application to create a file with a given size, and to");
    System.err.println("fill the file with a repeating pattern or pseudo-random data.");
    System.err.println();
    System.err.println("  java  CreateDummyFile2  [options]  fileSize  fileName");
    System.err.println();
    System.err.println("Two parameters are required on the command line.  The first parameter must be");
    System.err.println("the size of the file in bytes, as a decimal number, without commas or digit");
    System.err.println("grouping.  Suffixes are recognized for kilobytes, megabytes, etc.  The second");
    System.err.println("parameter is the output file name.  You may need to quote the name.  Options");
    System.err.println("select which data values to write for the bytes:");
    System.err.println();
    System.err.println("  -d# = one or more decimal bytes from 000 to 255");
    System.err.println("  -h# = one or more hexadecimal bytes from 00 to FF");
    System.err.println("  -o = write all ones, 0xFF bytes");
    System.err.println("  -p# = text pattern to repeat, in local character set");
    System.err.println("  -r = write pseudo-random data (default)");
    System.err.println("  -z = write all zeros, 0x00 bytes");
    System.err.println();
    System.err.println(COPYRIGHT_NOTICE);
//  System.err.println();

  } // end of showHelp() method

} // end of CreateDummyFile2 class

/* Copyright (c) 2024 by Keith Fenske.  Apache License or GNU GPL. */
