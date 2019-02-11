# Essemble

A tool to assemble essay PDFs from Markdown files and coversheet templates

## Why?

Many students and academics use Markdown to write their essays and papers. Unlike conventional word processors, this approach means one's source documents are text files, and changes can easily be tracked using version control systems like Git. However there's a downside - it's easy to copy and paste the coversheet to the front of one's essay using a word processor, but how do we do that if we're using Markdown? This is the problem Essemble tries to solve.

Essemble assembles essays from Markdown files and inserts a coversheet as the first page. The coversheet is populated from metadata in the Markdown file, as well as a word count. Since the information for the coversheet is in the Markdown file, this means it's tracked by your version control system, and can be edited using your normal text editing tools.

Since each university has their own coversheet, I've tried to come up with a process which makes it easy to create HTML templates from coversheets. Please contribute any templates you make back so other students from your university can benefit.

## Installation

### Requirements

Essemble requires the installation of some other tools before it will work. Specifically:

* [Ammonite](https://ammonite.io/), which is the Scala based scripting language Essemble is written in.
* [Pandoc](https://pandoc.org/), for generating PDFs from Markdown files (to convert the essay Markdown to a PDF).
* [Poppler](https://poppler.freedesktop.org), for the pdftotext and pdfunite utils. pdftotext is used for generating text files from PDFs (for word counting), while pdfunite is used for joining PDFs (to add the coversheet PDF to the front of the essay PDF).
* [wkhtmltopdf](https://wkhtmltopdf.org/), for generating high quality PDFs from HTML files (to convert the populated coversheet to a PDF).

#### Ubuntu

First, run the following command to install the other requirements:

```sh
sudo apt-get install default-jre pandoc poppler-utils wkhtmltopdf
```

Then, install Ammonite by following the instructions at <https://ammonite.io/#Ammonite-REPL>

#### Mac

First, install the Homebrew package manager by following the instructions at <https://brew.sh/#install>

Then, run the following command to install the other requirements:

```sh
brew install ammonite-repl pandoc poppler
brew cask install wkhtmltopdf
brew cask install basictex
```

### Putting Essemble in your PATH

Unless you don't mind specifying it's exact location every time you run it, you'll want to include Essemble's directory in your path. You should add a line like this `export PATH=$PATH:/wherever/you/put/essemble` to your shell's start up script (by default~/.bashrc on Ubuntu, or ~/.bash_profile on the Mac).

## How to use

### Write some Markdown

Write your essay in Markdown and save it somewhere. If you need help writing Markdown, look [here](https://pandoc.org/MANUAL.html#pandocs-markdown).

### Edit the essay's metadata

Right at the top of the Markdown file you should insert something like this (including the `---` lines):

```yaml
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
```

The text between the `---` lines uses the [YAML](https://yaml.org/) format. Essemble doesn't need the `documentclass`, `fontsize`, or `geometry` lines (these are used for styling by Pandoc), but it does need the `essemble` section. You can tell what values belong to this section because they are indented one level in with spaces. Most of these values are used to populate the coversheet template, so for instance `{{ module-title }}` in the template will be replaced by `Philosophy of Science`. The first 9 listed here are used by this particular template, so you can change them to match what you need to put in your coversheet. The last 4 are special, and hopefully self-explanatory. You can either specify `word-count` yourself, or leave it out and have Essemble generate it for you.

### Make a coversheet template or find one to reuse

At the moment Essemble only has a coversheet for King's College London's Faculty of Arts & Humanities. If that's useful to you, you should use the text `kcl.ac.uk/arts_humanities.html` for the value `coversheet-template-filename` in the essay's metadata. Otherwise you'll need to generate a template from your university's coversheet. To do this, you need to edit the coversheet in Word or whatever, and instead of inserting your real details you need to insert the names of the fields you want to replace, eg: `{{ module-title }}`. For King's College London's coversheet I needed the ability to index the values one character at a time, and you can do that by appending the field name with the index in square brackets, eg. `{{ candidate-number[0] }}`. The coversheet also had some checkboxes which I replaced with fields I give values like `☐` and `☑`. Save or export the file as HTML, and place it in a directory named after your university's domain name in Essemble's coversheets directory. Give it a name based on which subjects/faculty uses it, and then use the directory name and file name in the `coversheet-template-filename` value.

### Running it

It's a good idea to cd to the same directory as where your Markdown file is, and then run the following command:

```sh
essemble example-essay.md
```

By default, the resulting PDF will have the same name as the original Markdown file but with the .pdf extension. You can override that using the `result-filename` value in the metadata. The file will be created in your current directory.

I hope you find this program useful!

