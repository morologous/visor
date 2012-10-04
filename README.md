               
### What is Visor? 
Visor is a bridge API to help [Groovy](http://groovy.codehaus.org/) applications store and search information in [ElasticSearch](http://www.elasticsearch.org).  The intent of this API is to be as low-impact as possible for the user while still providing a high level of available customization.

### Get Visor

#### Github

Fork, clone or download this project at its [github page](http://github.com/morologous/visor)

#### Maven

Visor artifacts are available on the open source Sonatype repository at https://oss.sonatype.org
```xml
<dependency>
    <groupId>net.yankus</groupId>
    <artifactId>visor</artifactId>
    <version>0.1</version>
</dependency>
```

### How does Visor work?

Visor interacts with the ElasticSearch server using the [ElasticSearch Groovy API](http://www.elasticsearch.org/guide/reference/groovy-api/).   Visor is configured using class- and property-level annotations.

Let's say you have a bean like this:

```groovy
class Book {
   def isbn
   def title
   def publishDate
   def author
   def pages
   def categories
}                 
```
To make this bean searchable using Visor, judiciously add Visor, Id and Field annotations

```groovy
@Visor(index='books')
class Book {
   @Id
   def isbn
   @Field
   def title
   @Field(type=Date)
   def publishDate
   @Field
   def author
   @Field
   def pages
   @Field
   def categories
}
```
In this example, we've annotated the appropriate fields.  One important note is the publishDate field.  To unlock some additional functionality for date searching the type property has been set on the Field annotation.  This hints Visor to watch out for Date data or Date range type searches.  More on that later.

When the Visor annotation is added to the bean several instance methods are added.  These are:

* **book.index()**   add this object to the index
* **book.search()**   search for objects like this one in the index
* **book.update()**   convenience method for reindexing data, semantically the same as .index()
* **book.delete()**   remove this object (by id) from the index

#### Putting it all together

Now that we've configured Visor, we can start working with the index:

```groovy
// create a representative book
def book = new Book(isbn:'0-684-84328-5', 
                    title:'Visor Style Guide: Dangers and Pitfalls',
                    publishDate: parseDate('2010-01-31'),
                    author: 'Cornelius D. Bagg',
                    pages: 235,
                    categories: ['self-help', 'fashion'])

// store the book                   
book.index()

// search for other books by the same author
def results = new Book(author:'*D. Bagg').search()

// print the results
println "$results.count books by Cornelius D. Bagg:"
results.list.each {
    println it
}
```

#### Special Searches

##### Collections

Visor can search for beans that match a parameter in a list (like a SQL IN clause).  To perform this kind of search, use the MultiSelect object.  Here is an example:

```groovy
// search for books in the fashion or fitness category
def results = new Book(
    categories: new MultiSelect(values:['fashion', 'fitness'])
).search()

// print the results
println "$results.count books returned from search:"
results.list.each {
    println it
}
```

##### Date Ranges

Visor can perform Date Range searches as well.  When performing a date range search simply substitute the DateRange object for the Date in the bean.  Here is an example of a 'Between' range query:

```groovy
// search for books published in 2010.
def results = new Book(
    publishDate: new DateRange(from: parseDate('2010-01-01'), 
                               to:   parseDate('2010-12-31')
).search()

// print the results
println "$results.count books returned from search:"
results.list.each {
    println it
}
```

For 'Before' or 'After' simply omit the 'from' property (for 'Before' queries) or the 'to' property (for 'After' queries).  In all cases the search will be inclusive of the 'to' and 'from' dates, so consider the time values provided for the dates accordingly.

##### Query String

The Visor annotation also adds a 'queryString' property to the bean, for free form text searching of the documents in the index.

```groovy
// search for books with titles
def results = new Book(queryString:'Pitfalls OR Pratfalls').search()

println "$results.count books returned from search:"
results.list.each {
    println it
}
```

The query string syntax adheres to the [Lucene Query Syntax](http://lucene.apache.org/core/3_6_0/queryparsersyntax.html) rules.  By default, the all fields in the document will be searched. 

[![Visor](https://github.com/morologous/visor/raw/master/site/visor.png)](http://morologous.github.com/visor)

[![Build Status](https://buildhive.cloudbees.com/job/morologous/job/visor/badge/icon)](https://buildhive.cloudbees.com/job/morologous/job/visor/)