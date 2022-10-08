#!/usr/bin/env python3 

import argparse
import csv
import os
import sys
import random

parser = argparse.ArgumentParser(description="Generate batches from a CSV file")
parser.add_argument('filepath', metavar='FILE-PATH', type=str, help='csv-file to generate batches from')
parser.add_argument('--input-sep', metavar='SEP-CHAR', type=str, default=";", help='separator ("," or ";") which will be used for parsing the csv-file (default: ";")')
parser.add_argument('--max-output', metavar='SIZE-MB', type=int, default=10000, help='limits the maximum amount of megabytes the program may generate (default: 10000)')
parser.add_argument('--repeat', action='store_true', help='repeat rows once the file is fully read'),
parser.add_argument('--mutate-on-repeat', action='store_true', help='mutate cell values on row repetitions'),
parser.add_argument('--delete-chance', metavar='DELETE-CHANCE', type=float, default=0.0, help='chance (0.0 to 1.0) a previous row will be deleted instead of a new one generated (default: 0_')
parser.add_argument('--batch-size', metavar='SIZE-KB', type=float, default=64, help='chance (0.0 to 1.0) a previous row will be deleted instead of a new one generated (default: 64)')
parser.add_argument('--output-sep', metavar='SEP-CHAR', type=str, default=";", help='separator ("," or ";") which will be used for generating the batch (default: ";")')

class CSVRowReader:
    """takes a CVS-File and return the rows together with an index as arrays
    """
    def __init__(self, filepath: str, input_sep: str):
        """Constructor method
        """
        self.filepath = filepath
        self.input_sep = input_sep
        self.reader = csv.reader(open(filepath, encoding='utf-8'), delimiter=input_sep)
        self._attributes = next(self.reader)
        self.index = -1
    
    def attributes(self):
        """returns the attributes (first line of File) of the dataset.

        :return: Index '$' + attribute names
        :rtype: Array[String]
        """
        return ['$'] + self._attributes

    def nextRow(self):
        """returns an index with a row of the CSV-file. 
        If this is not possible (e.g. no more row left) the iteration stops and returns None

        :return: as long as possible: Index + row of reader. If no longer possible: None
        :rtype: Array[String]
        """
        try:
            self.index += 1
            return [str(self.index)] + [c.strip() for c in next(self.reader)]
        except StopIteration:
            return None
    
    def reset(self):
        """resets the reader and the index and calls the first line again.
        """
        self.reader = csv.reader(open(self.filepath, encoding='utf-8'), delimiter=self.input_sep)
        self.index = -1
        next(self.reader)


class RowRepeater:
    """takes the reader (inner) and repeats the rows indefinitely
    """
    def __init__(self, inner, mutate: bool):
        """Constructor method
        """
        self.inner = inner
        self.mutate = mutate
        self.nth_row = 0
        self.nth_repeat = 0
    
    def attributes(self):
        """returns the attribute of the passed reader

        :return: calls the attribute function of the passed generator
        :rtype: Array[String]
        """
        return self.inner.attributes()

    def nextRow(self):
        """call the nextRow def of the given reader and decrease remaining_rows by 1
                if it returns "None" than it call the reset def of the given reader 
                call the readers nextRow def again
            the index gets adjusted and the row gets returned

        :return: None or Index + row of reader
        :rtype: None or Array[String]
        """
        next_row = self.inner.nextRow()
        if next_row is None:
            self.inner.reset()
            next_row = self.inner.nextRow()
            self.nth_repeat += 1

        if self.mutate and self.nth_repeat >= 1:
            # NOTE first cell will be overwritten anyways
            next_row = [c + f"${self.nth_repeat}" for c in next_row]

        next_row[0] = str(self.nth_row)
        self.nth_row += 1
        return next_row
    
    def reset(self):
        """resets the RowRepeater by calling for the reset def of the given reader 
        """
        self.inner.reset()
        self.nth_row = 0
        self.nth_repeat = 0


class RowDeleter:
    """deleats a row of a given row_generator with a specified delete chance
    """
    def __init__(self, inner, delete_chance: float):
        """Constructor method
        """
        self.inner = inner
        self.delete_chance = delete_chance
        self.max_known_index = 0

    def attributes(self):
        """returns the attribute of the passed row-generator

        :return: calls the attribute function of the passed generator
        :rtype: Array[String]
        """
        return self.inner.attributes()

    def nextRow(self):
        """Chooses randomly a number between 0 and 1. 
        If this number ist smaller than the given delete chance a randomly chosen row gets deleted.
        Else a new row is returned.

        :return: Either an "old" Index and an empty String for each attribute or a completely new row
        :rtype: Array[String]
        """
        if random.uniform(0, 1) < self.delete_chance:
            return [str(random.randint(0, self.max_known_index))] + ([''] * (len(self.attributes())-1))
        else:
            row = self.inner.nextRow()
            if row is not None:
                self.max_known_index = max(self.max_known_index, int(row[0]))
            return row
    
    def reset(self):
        """resets the deleter and calls the reset def from the given row-generator.
        """
        self.max_known_index = 0
        self.inner.reset()


class Batcher:
    """takes a row_generator (an instance of upper classes) and generates a batch with a specified maximum size
    """
    def __init__(self, row_generator, max_output: int, batch_size: int, output_sep: str):
        """Constructor method

        :param row_generator: can ether be an instance of CSVRowReader, RowRepeater or RowDeleter
        :param max_output: maximum size of total output in bytes
        :param batch_size: maximum size of the batch in bytes
        :type batch_size: int
        """
        self.row_generator = row_generator
        self.remaining_bytes = max_output
        self.batch_size = batch_size
        self.output_sep = output_sep
    
    def nextBatch(self):
        """calls the attribute def and joined them with commas. 
        As long as the length of the batch is smaler then the batch_size 
            the nextRow dev of the given row_generator gets called and the individual strings of each row gets joind with a comma
        At last the the attribute string and the batch gets connected

        :return: A list of rows where the rows are seperated by a new line 
        and the entries of each row sepersted by comma and 
        :rtype: String
        """
        batch = ''
        while len(batch) < self.batch_size:
            row = self.row_generator.nextRow()
            if row is None:
                break
            batch += '\n' + self.output_sep.join(row)
        self.remaining_bytes -= len(batch)
        if self.remaining_bytes <= 0 or batch == '':
            return None
        return self.output_sep.join(self.row_generator.attributes()) + batch


class CSVReadIn:
    """takes the location of a CSV-file and a number of parameters (see argparse config).
    creates a reader, then a repeater, then a deleter and than give it to the Batcher 
    creates a batch that can be submitted to the system
    """
    def __init__(
            self,
            filepath: str,
            input_sep: str,
            output_sep: str,
            max_output: int,
            repeat: bool,
            mutate: bool,
            delete_chance: float,
            batch_size: int):
        """Constructor method
        """
        self.filepath = filepath
        self.input_sep = input_sep
        self.output_sep = output_sep
        self.max_output = max_output
        self.repeat = repeat
        self.mutate = mutate
        self.delete_chance = delete_chance
        self.batch_size = batch_size
    
    def openRowGenerator(self):
        """creates a instance of RowDeleter (deleter) by creating a reader (instance of CSVRowReader) and than a 
        repeater (instance of RowRepeater).

        :return: a deleter with the given deleat_chance and a repeater
        """
        reader = CSVRowReader(self.filepath, self.input_sep)
        if self.repeat:
            reader = RowRepeater(reader, self.mutate)
        if self.delete_chance > 0.0:
            reader = RowDeleter(reader, self.delete_chance)
        
        return reader
    
    def run(self):
        """Calls the Batcher class with a configurable batch_size
        
        Reads out batches to stdout, multiple batches separated with an empty line
        """
        batcher = Batcher(self.openRowGenerator(), self.max_output, self.batch_size, self.output_sep)
        batch = batcher.nextBatch()
        while batch is not None:
            print(batch)
            print() # empty line to separate batches
            batch = batcher.nextBatch()

if __name__ == '__main__':
    args = parser.parse_args()
    read_in = CSVReadIn(
        filepath=args.filepath,
        input_sep=args.input_sep,
        output_sep=args.output_sep,
        max_output=args.max_output * 1024 * 1024,
        repeat=args.repeat,
        mutate=args.mutate_on_repeat,
        delete_chance=args.delete_chance,
        batch_size=args.batch_size * 1024,
    )
    read_in.run()



######## HTTP Poster wird nicht mehr gebraucht

# class HTTPPoster:
#     def __init__(self):
#         self.conn = http.client.HTTPConnection(sys.argv[1])
#         self.goal = sys.argv[1] #wo hin soll die CSV
#         self.adress_csv = sys.argv[2] #Pfad wo die Datei liegt
#         self.number_of_rows = int(sys.argv[3])
    
#     def openRowRepeater(self):
#         reader = CSVRowReader(self.adress_csv)
#         repeater = RowRepeater(self.number_of_rows, reader)
#         return repeater
    
#     def run(self):
#         batcher = Batcher(self.openRowRepeater(), 1000)
#         batch = batcher.nextBatch()
#         while batch is not None:
#             self.conn.request("POST", "/", batch)
#             batch = batcher.nextBatch()
#             time.sleep(2.4)




# reader1=CSVRowReader('/Users/ilove/OneDrive/Uni/Master - Philipps Uni/1. Semester/Projektarbeit/Projektarbeit/DynamicDataProfile-1/datagen/adressen.csv')
# reader=RowRepeater(12, reader1)

# row = reader.nextRow()
# attributes = reader.attributes()
# print(attributes)
# while row:
#     print(row)
#     row = reader.nextRow()

# reader.reset()
# attributes = reader.attributes()
# row = reader.nextRow()
# while row:
#     print(row)
#     row = reader.nextRow()

# batcher = Batcher(reader,100)
# print(batcher.nextBatch())
# print(batcher.nextBatch())

# readin = CSVReadIn()
# readin.run()

# print(sys.argv)
