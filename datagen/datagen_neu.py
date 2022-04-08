import csv
import sys
import random
import http
import time

#reads in CVS-File and return the rows as arrays 
class CSVRowReader:
    def __init__(self, filename):
        self.reader = csv.reader(open(filename, encoding='utf-8'),delimiter=',')
        self._attributes = next(self.reader)
        self.filename = filename
        self.index = -1
    
    def attributes(self):
        return ['$'] + self._attributes

    def nextRow(self):
        try:
            self.index += 1
            return [str(self.index)] + next(self.reader)
        except StopIteration:
            return None
    
    def reset(self):
        self.reader = csv.reader(open(self.filename, encoding='utf-8'),delimiter=',')
        self.index = -1
        next(self.reader)


class RowRepeater:
    def __init__(self, max_row, inner):
        self.max_row = max_row
        self.inner = inner
        self.remaining_rows = max_row
    
    def attributes(self):
        return self.inner.attributes()

    def nextRow(self):
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
        self.remaining_rows = self.max_row
        self.inner.reset()


class Batcher:
    def __init__(self, row_generator, max_batch_size):
        self.row_generator = row_generator
        self.max_batch_size = max_batch_size
    
    def nextBatch(self):
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


class RowDeleter:
    def __init__(self, delete_chance, row_generator):
        self.delete_chance = delete_chance
        self.row_generator = row_generator
        self.max_index = 0

    def attributes(self):
        return self.row_generator.attributes()

    def nextRow(self):
        if random.uniform(0, 1) < self.delete_chance:
            return str(random.randint(0, self.max_index)) + (","*(len(self.attributes())-1))
        else:
            row = self.row_generator.nextRow()
            if row is not None:
                self.max_index = max(self.max_index,int(row[0]))
            return row
    
    def reset(self):
        self.max_index = 0
        self.row_generator.reset()


class CSVReadIn:
    def __init__(self):
        self.goal = sys.argv[1] #wo hin soll die CSV
        self.adress_csv = sys.argv[2] #Pfad wo die Datei liegt
        self.number_of_rows = int(sys.argv[3])
        self.delete_chance = 0.1
    
    def openRowGenerator(self):
        reader = CSVRowReader(self.adress_csv)
        repeater = RowRepeater(self.number_of_rows, reader)
        deleter = RowDeleter(self.delete_chance, repeater)
        return deleter
    
    def run(self):
        batcher = Batcher(self.openRowGenerator(), 1000)
        batch = batcher.nextBatch()
        while batch is not None:
            print(batch)
            batch = batcher.nextBatch()


class HTTPPoster:
    def __init__(self):
        self.conn = http.client.HTTPConnection(sys.argv[1])
        self.goal = sys.argv[1] #wo hin soll die CSV
        self.adress_csv = sys.argv[2] #Pfad wo die Datei liegt
        self.number_of_rows = int(sys.argv[3])
    
    def openRowRepeater(self):
        reader = CSVRowReader(self.adress_csv)
        repeater = RowRepeater(self.number_of_rows, reader)
        return repeater
    
    def run(self):
        batcher = Batcher(self.openRowRepeater(), 1000)
        batch = batcher.nextBatch()
        while batch is not None:
            self.conn.request("POST", "/", batch)
            batch = batcher.nextBatch()
            time.sleep(2.4)




reader1=CSVRowReader('/Users/ilove/OneDrive/Uni/Master - Philipps Uni/1. Semester/Projektarbeit/Projektarbeit/DynamicDataProfile-1/datagen/adressen.csv')
reader=RowRepeater(12, reader1)

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

readin = CSVReadIn()
readin.run()

# print(sys.argv)