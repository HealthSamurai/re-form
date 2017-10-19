# re-form

[![Build Status](https://travis-ci.org/HealthSamurai/re-form.svg?branch=master)](https://travis-ci.org/HealthSamurai/re-form)

A ClojureScript form toolbox


## Usage

FIXME

## License

Copyright © 2017 Health-Samurai

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

## Questions
Can we allow library users use re-frame events or this events internal to library?
For example, we have colleciton. Items only need to delete or add not change. We have to options. First, we create new input that treat the 'value' as vector. On 'on-change' we can 'conj' item or remove. Second, we allow user use re-frame subscription on collection and use re-frame events to add or delete items in this collection.