```
                          .     t#,               
             t           ;W    ;##W.   j.         
             Ej         f#E   :#L:WE   EW,        
  t      .DD.E#,      .E#f   .KG  ,#D  E##j       
  EK:   ,WK. E#t     iWW;    EE    ;#f E###D.     
  E#t  i#D   E#t    L##Lffi f#.     t#iE#jG#W;    
  E#t j#f    E#t   tLLG##L  :#G     GK E#t t##f   
  E#tL#i     E#t     ,W#i    ;#L   LW. E#t  :K#E: 
  E#WW,      E#t    j#E.      t#f f#:  E#KDDDD###i
  E#K:       E#t  .D#j         f#D#;   E#f,t#Wi,,,
  ED.        E#t ,WK,           G#t    E#t  ;#W:  
  t          E#t EG.             t     DWi   ,KK: 
             ,;. ,                                
```
[![Build Status](https://secure.travis-ci.org/morologous/visor.png)](http://travis-ci.org/morologous/visor)
                        
### Visor
Visor is a bridge API to help Groovy applications store and search information in ElasticSearch.  The intent of this API is to be as low-impact as possible for the user while still providing a high level of available customization.

### Get Visor

Get Visor from github or by using Maven

### Use Visor

Visor configuration is Java Annotation-based.  The Visor class annotation is used to specify ElasticSearch client configuration as well as any filters that should be applied to every search request.  The Visor annotation will add following convenience methods to the annotated bean

* search()
* index()
* delete()
* update()

For each searchable field in the bean, add a Field annotation to the property.  Most basic fields won't need any additional configuration, but Dates and Collections need to have the 'type' property of the Annotation set.  For Date properties, the type value should be Date.  For Collection properties, set the type value to the Class of the elements stored in the collection.  The type value is used when the data is marshalled into the ElasticSearch query or unmarshalled from the search response.

