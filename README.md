               
### Visor 
Visor is a bridge API to help Groovy applications store and search information in ElasticSearch.  The intent of this API is to be as low-impact as possible for the user while still providing a high level of available customization.

### Get Visor

Get Visor from github or by using Maven

### How does it work?

Visor interacts with the ElasticSearch server using it's [Groovy API](http://www.elasticsearch.org/guide/reference/groovy-api/).   Visor is configured using class- and property-level annotations.

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

Visor can perform Date Range searches as well.  When performing a date range search simply substitute the DateRange object for the Date in the bean.  Here is an example:

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

[![Visor](https://github.com/morologous/visor/raw/master/site/visor.png)](http://morologous.github.com/visor)

[![Build Status](https://secure.travis-ci.org/morologous/visor.png)](http://travis-ci.org/morologous/visor)