# MySQL Timezone Related Examples

This project contains test codes to demonstrate how to properly work with timezone sensitive data types like timestamps,
both on the application side and the MySQL server side. It also contains test code samples to demonstrate how current
timezone value of SimpleDateFormat or JVM's default timezone value affects parsing and formatting of the date values.
Similar test cases exist for JacksonObjectMapper's JSON object serialization process as well.

### How using a daylight saving timezone might cause loss of value accuracy?

When working with a daylight saving timezone, such as America/Los_Angeles, there are two time points every year at which 
causes moving the clock either 1 hour backward or 1 hour forward, and if your application uses a daylight saving timezone, its
timestamp values will get affected at those times. Loss of value accuracy occurs when the clock is moved 1 hour backward, 
and clients which are working with a non-daylight saving timezone such as GMT/UTC, sending timestamp values during that 
1-hour period.

![](https://github.com/ksevindik/mysql-timezone-examples/blob/master/images/pdt_timezone_data_accuracy_issue.png =150x250)

On the other hand, when clock is moved 1-hour ahead, there happens no data accuracy loss, there will exist on data having
timestamp values during this period, which might be a bit confusing if you examine the data with the daylight saving timezone, 
instead of GMT/UTC.

![](https://github.com/ksevindik/mysql-timezone-examples/blob/master/images/pdt_timezone_one_hour_ahead.png =150x250)

### Why do we get errors like "Data truncation: Incorrect datetime value: '2022-03-13 02:01:00' for column 'ts_value' at row 1" ?
If the MySQL server operates with a daylight saving timezone like America/Los_Angeles, and the application which sends 
a timestamp value which corresponds to non-existing time period for that DST timezone, such as 02:00-02:59 on 13th March 
2022 for PDT, MySQL server wouldn't be able to process this value on its side and raises the error. The root cause for
this problem is that application side doesn't serialize the timestamp value with the timezone of the MySQL server, for
example, it might serialize the value with GMT/UTC timezone, which causes no time conversion during this serialization,
and then MySQL server attempts to deserialize this value and tries to reconstruct timestamp value, however, as the value
belongs to a non-existent time period in MySQL server's timezone, it fails during this step, and raises the error with
its message "Data truncation: Incorrect datetime value".

In order to avoid "Data truncation: Incorrect datetime value" errors, your application should employ the exact
timezone corresponding to the MySQL server's timezone during this serialization process, in fact setting `connectionTimezone` or
`serverTimezone`JDBC url parameters with the MySQL server's timezone value will do this.