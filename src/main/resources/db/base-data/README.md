# Base Data for OpenLMIS Auth Service

This folder holds the base layer of this service's test data: OAuth clients, the service API key,
and the logins of the users named in the referencedata base layer.

Each data profile (`base-data`, `demo-data`, `functional-test-data`, `edge-case-data`,
`performance-data`) loads `base-data` first, then the layer it names. Rows of a layer refer only to
rows of their own layer or of `base-data`, in this service and in referencedata, whose layers these
follow.
