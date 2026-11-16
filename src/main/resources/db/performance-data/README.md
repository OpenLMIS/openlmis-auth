# Performance Data for OpenLMIS Auth Service

This folder holds the `performance-data` layer of this service's test data: logins of the generated
referencedata users without roles, which are in the referencedata `performance-data` layer.

Each data profile (`base-data`, `demo-data`, `functional-test-data`, `edge-case-data`,
`performance-data`) loads `base-data` first, then the layer it names. Rows of a layer refer only to
rows of their own layer or of `base-data`, in this service and in referencedata, whose layers these
follow.
