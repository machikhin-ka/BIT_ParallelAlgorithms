# Task 1: Parallel Quick Sort
***
Параллельный фильтр и быстрая сортировка реализованы на Fork/Join Framework Java. 
***
### Структура работы:
+ quicksort.SerialAlgorithms: serial_scan, serial_filter, serial_quick_sort
+ quicksort.ParallelAlgorithms: parallel_for, blocked_for, parallel_scan, parallel_filter, parallel_map
+ quicksort.ParallelQuickSort: реализация parallel quick sort
***
###Замеры производительности: <br/>

| Serial | 1 thread | 2 thread | 3 thread | 4 thread
|:----|:----|:----|:----|:----|
| 5894ms | 6917ms | 4448ms | 3853ms | 5235ms

###Отношение времени выполнения последовательного алгоритма к параллельному: <br/>
1. `Serial`/`1Thread` = 0,85
2. `Serial`/`2Thread` = 1,32
3. `Serial`/`3Thread` = 1,52
4. `Serial`/`4Thread` = 1,80