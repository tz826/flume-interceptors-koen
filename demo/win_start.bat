::flume-ng.cmd agent --conf ../conf --conf-file ../conf/agent.conf --name dev -property flume.root.logger=INFO,console
::放置在bin目录下,调试时添加到flume-parent/bin/ 目录下
flume-ng.cmd agent -conf ../conf -f ../conf/flume-debug.conf -n a1 -property flume.root.logger=INFO,console;flume.monitoring.type=http;flume.monitoring.port=34545

pause