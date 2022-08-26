#!/usr/bin/env python3 

import csv
import sys
import random
import http         # nur relevant für HTTP Poster
import time         # nur relevant für HTTP Poster



class CSVRowReader:
    """takes a CVS-File and return the rows together with an index as arrays
    """
    def __init__(self, filename):
        """Constructor method

        :param filename: location of a CSV file
        :type filename: CSV file
        """
        self.reader = csv.reader(open(filename, encoding='utf-8'),delimiter=',')
        self._attributes = next(self.reader)
        self.filename = filename
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
        except StopIteration:
            return None
    
    def reset(self):
        """resets the reader and the index and calls the first line again.
        """
        self.reader = csv.reader(open(self.filename, encoding='utf-8'),delimiter=',')
        self.index = -1
        next(self.reader)


class RowRepeater:
    """takes the reader (inner) and repeats the rows until a specified number of maximum rows is reached
    """
    def __init__(self, max_row, inner):
        """Constructor method

        :param max_row: number of row to returned
        :type max_row: int
        :param inner: RowRepeater is a inner-funtion of the CSVRowReader
        """
        self.max_row = max_row
        self.inner = inner
        self.remaining_rows = max_row
    
    def attributes(self):
        """returns the attribute of the passed reader

        :return: calls the attribute function of the passed generator
        :rtype: Array[String]
        """
        return self.inner.attributes()

    def nextRow(self):
        """If remaining_rows is not equal to 0: 
            call the nextRow def of the given reader and decrease remaining_rows by 1
                if it returns "None" than it call the reset def of the given reader 
                call the readers nextRow def again
            the index gets adjusted and the row gets returned
        Else retun None

        :return: None or Index + row of reader
        :rtype: None or Array[String]
        """
        if self.remaining_rows != 0:
            next_row = self.inner.nextRow()
            self.remaining_rows -= 1
            if next_row is None:
                self.inner.reset()
                next_row = self.inner.nextRow()
            next_row[0] = str(self.max_row - self.remaining_rows -1)
            return next_row
        else: 
            return None
    
    def reset(self):
        """resets the RowRepeater by calling for the reset def of the given reader 
        and resets the remeining_rows to the max_row again
        """
        self.remaining_rows = self.max_row
        self.inner.reset()


class RowDeleter:
    """deleats a row of a given row_generator with a specified delete chance
    """
    def __init__(self, delete_chance, row_generator):
        """Constructor method

        :param delete_chance: a number between 0 and 1 
        :type delete_chance: float
        :param row_generator: can be eather a CSVRowReader or a RowRepeater
        """
        self.delete_chance = delete_chance
        self.row_generator = row_generator
        self.max_index = 0

    def attributes(self):
        """returns the attribute of the passed row_generator

        :return: calls the attribute function of the passed generator
        :rtype: Array[String]
        """
        return self.row_generator.attributes()

    def nextRow(self):
        """Chooses randomly a number between 0 and 1. 
        If this number ist smaller than the given delete chance a randomly chosen row gets deleted.
        Else a new row is returned.

        :return: Either an "old" Index and an empty String for each attribute or a completely new row
        :rtype: Array[String]
        """
        if random.uniform(0, 1) < self.delete_chance:
            return [str(random.randint(0, self.max_index))] + ([''] * (len(self.attributes())-1))
        else:
            row = self.row_generator.nextRow()
            if row is not None:
                self.max_index = max(self.max_index,int(row[0]))
            return row
    
    def reset(self):
        """resets the deleter and calls the reset def from the given row_generator.
        """
        self.max_index = 0
        self.row_generator.reset()


class Batcher:
    """takes a row_generator (an instance of upper classes) and generates a batch with a specified maximum size
    """
    def __init__(self, row_generator, max_batch_size):
        """Constructor method

        :param row_generator: can ether be an instance of CSVRowReader, RowRepeater or RowDeleter
        :param max_batch_size: maximum size of the batch 
        :type max_batch_size: int
        """
        self.row_generator = row_generator
        self.max_batch_size = max_batch_size
    
    def nextBatch(self):
        """calls the attribute def and joined them with commas. 
        As long as the length of the batch is smaler then the max_batch_size 
            the nextRow dev of the given row_generator gets called and the individual strings of each row gets joind with a comma
        At last the the attribute string and the batch gets connected

        :return: A list of rows where the rows are seperated by a new line 
        and the entries of each row sepersted by comma and 
        :rtype: String
        """
        batch = ''
        attributes = self.row_generator.attributes()
        attr_string = ','.join(attributes)
        while len(batch) < self.max_batch_size:
            row = self.row_generator.nextRow()
            if row is None:
                break
            row_string = ','.join(row)
            batch += '\n' + row_string
        if batch == '':
            return None
        return attr_string + batch


class CSVReadIn:
    """takes the location of a CSV-file and number of rows, 
    creates a reader, then a repeater, then a deleter and than give it to the Batcher 
    creates a batch that can be submitted to the system
    """
    def __init__(self):
        """Constructor method
        """
        self.goal = sys.argv[1] #wo hin soll die CSV
        self.adress_csv = sys.argv[2] #Pfad wo die Datei liegt
        self.number_of_rows = int(sys.argv[3])
        self.delete_chance = 0.1
    
    def openRowGenerator(self):
        """creates a instanc of RowDeleter (deleter) by creating a reader (instanc of CSVRowReader) and than a 
        repeater (instance of RowRepeater).

        :return: a deleter with the given deleat_chance and a repeater
        """
        reader = CSVRowReader(self.adress_csv)
        repeater = RowRepeater(self.number_of_rows, reader)
        deleter = RowDeleter(self.delete_chance, repeater)
        return deleter
    
    def run(self):
        """Calls the Batcher class with a max_batch_size of 1000
        """
        batcher = Batcher(self.openRowGenerator(), 1000)
        batch = batcher.nextBatch()
        while batch is not None:
            print(batch)
            batch = batcher.nextBatch()




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