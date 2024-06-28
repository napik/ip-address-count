# IP Address Counter

## Overview

The IP Address Counter is a Kotlin-based application designed to read a file containing IP addresses, count the unique
addresses, and output the result. The application is optimized for performance using multithreading and efficient data
structures.

## Features

- Read IP addresses from a file using memory buffer.
- Uses a custom IpAddressVector to store unique IP addresses efficiently.
- Multithreaded reading and processing using virtual threads and CompletableFuture.
- Optimized file reading in chunks for large files.
- Implement a subscriber-publisher pattern for processing file chunks.

## Usage

- The FileReader class is responsible for reading the file in chunks and passing each chunk to a subscriber for
  processing.
- The IpAddressVector class stores unique IP addresses using a bit vector.
- The ScanChunkSubscriber class processes each chunk of data, extracts IP addresses, and updates the IpAddressVector.
- The application uses a subscriber-publisher pattern to efficiently handle file reading and processing:
    - FileReader acts as the publisher, reading chunks of the file and publishing them to subscribers.
    - ScanChunkSubscriber acts as the subscriber, receiving chunks from the FileReader and processing them to extract IP
      addresses.

## Prerequisites

JDK 17 or higher.<br>
Kotlin 2.0 or higher.

## Run

java -jar ip-address-counter.jar <filename>

Where ***filename*** is the full path to the file containing the IP addresses.
