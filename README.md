# codescene-jenkins-plugin

[![Build Status](https://travis-ci.org/swehacker/codescene-jenkins-plugin.svg)](https://travis-ci.org/swehacker/codescene-jenkins-plugin)

A jenkins plugin for
[CodeScene](http://www.empear.com/products/codescene-on-premise/) by Empear.


CodeScene detects potential maintenance problems and early warnings in your
codebase. The earlier you can react to those findings, the better. That’s why
CodeScene offers integration points that let you incorporate the analysis
results into your build pipeline.

This plugin lets you use CodeScene’s Delta Analysis to catch potential problems
before they are delivered to your main branch.

![Screenshot](screenshot.png)

## Changelog

* 0.1.0
  - Delta analysis by branch, based on _base revision_
  - Delta analysis by individual commits (new commits not seen in previous jobs)
  - "Mark build as unstable" based on risk threshold

## Contributing

You're encouraged to submit [pull
requests](https://github.com/swehacker/codescene-jenkins-plugin/pulls),
and to [propose features and discuss
issues](https://github.com/swehacker/codescene-jenkins-plugin/issues).

## License

Licensed under the [MIT License](LICENSE).
