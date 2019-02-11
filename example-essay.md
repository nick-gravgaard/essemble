---
documentclass: extarticle
fontsize: 12pt
geometry: top=3cm, bottom=3cm, left=3cm, right=3cm
essemble:
    candidate-number: "A12345"
    module-title: "Philosophy of Science"
    module-code: "5AABC123"
    assignment: "Assignment"
    assignment-tutor-group: "Dr John Smith"
    deadline: "2018-12-31 16:00"
    date-submitted: "2018-12-30"
    make-public-yes: "☐"
    make-public-no: "☑"
    coversheet-template-filename: "kcl.ac.uk/arts_humanities.html"
    count-until: "Bibliography"  # if this line exists, words will be counted until a line with this text is found, else all words will be counted
    #word-count: "12345"  # uncomment this line if you disagree with Essemble's word count number
    result-filename: "result1234.pdf"  # if this line is commented out, the filename will be based on the Markdown file
---

# An h1 header

Paragraphs are separated by a blank line.

2nd paragraph. *Italic*, **bold**, and `monospace`. Itemized lists
look like:

* this one
* that one
* the other one

> Block quotes are
> written like so.
>
> They can span multiple paragraphs,
> if you like.

Use 3 dashes for an em-dash. Use 2 dashes for ranges (ex., "it's all
in chapters 12--14"). Three dots ... will be converted to an ellipsis.

## An h2 header

Here's a numbered list:

1. first item
2. second item
3. third item

Here's a code sample:

    # Let me re-iterate ...
    for i in 1 .. 10 { do-something(i) }

Instead of indenting the block, you can use delimited blocks, if you
like:

~~~
define foobar() {
    print "The future ain't what it used to be.";
}
~~~

(which makes copying & pasting easier). You can optionally mark the
delimited block for Pandoc to syntax highlight it:

~~~scala
object FizzBuzz extends App {
  1 to 100 foreach { n =>
    println((n % 3, n % 5) match {
      case (0, 0) => "FizzBuzz"
      case (0, _) => "Fizz"
      case (_, 0) => "Buzz"
      case _ => n
    })
  }
}
~~~

### An h3 header

Now a nested list:

1. First, get these ingredients:

    * carrots
    * celery
    * lentils

2. Boil some water.

3. Dump everything in the pot and follow
    this algorithm:

        find wooden spoon
        uncover pot
        stir
        cover pot
        balance wooden spoon precariously on pot handle
        wait 10 minutes
        goto first step (or shut off burner when done)

    Do not bump wooden spoon or it will fall.

Notice how text always lines up on 4-space indents (including that
last line which continues item 3 above).

Here's a link to [a website](http://foo.bar), to a [local
doc](local-doc.html), and to a [section heading in the current
doc](#an-h2-header). Here's a footnote [^1].

[^1]: Some footnote text.

Tables can look like this:

Name           Size  Material      Color
------------- -----  ------------  ------------
All Business      9  leather       brown
Roundabout       10  hemp canvas   natural
Cinderella       11  glass         transparent

Table: Shoes sizes, materials, and colors.

(The above is the caption for the table.) Pandoc also supports
multi-line tables:

--------  -----------------------
Keyword   Text
--------  -----------------------
red       Sunsets, apples, and
          other red or reddish
          things.

green     Leaves, grass, frogs
          and other things it's
          not easy being.
--------  -----------------------

A horizontal rule follows.

---

Here's a definition list:

apples
: Good for making applesauce.

oranges
: Citrus!

tomatoes
: There's no "e" in tomatoe.

Put a blank line between each term and  its definition to spread
things out more.

Here's a "line block" (note how whitespace is honoured):

| Line one
|   Line too
| Line tree

This is the last line that will be included in the word count.

## Bibliography

Chakravartty, Anjan. "Scientific Realism", *The Stanford Encyclopedia of Philosophy* (Winter 2018 Edition), Edward N. Zalta (ed.). <https://plato.stanford.edu/archives/win2018/entries/scientific-realism/> (accessed December 30, 2017)

Feyerabend, Paul, *Against Method*, 3rd revised edition, London: Verso, 1993.

Hacking, Ian. *Representing and Intervening: Introductory Topics in the Philosophy of Natural Science*. Cambridge: Cambridge University Press, 1983.

Heisenberg, Werner. *Physics and Beyond, Encounters and Conversations*. New York: Harper & Row, 1971.

Popper, Karl. *Objective Knowledge: An Evolutionary Approach*, Oxford: Oxford University Press, 1979.

Russell, Bertrand. "The Limits of Empiricism", *Proceedings of the Aristotelian Society, New Series, Vol. 36 (1935 - 1936)*, pp. 131-150, Oxford: Oxford University Press on behalf of The Aristotelian Society

