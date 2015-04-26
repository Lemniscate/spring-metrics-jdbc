# Spring-Metrics-JDBC

A simple JDBC-backed `MetricRepository` that asynchronously updates metric values. We opted for this approach as we
wanted to aggregate all our metrics to our database and didn't want to coordinate exporters across our cluster.

Still very much a proof-of-concept