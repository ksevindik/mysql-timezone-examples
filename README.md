# How to Work with MySQL Timezone Sensitive Data Type: Timestamp Properly?

This project contains test cases to demonstrate how to properly work with timezone sensitive timestamp data type,
both on the application side and on the MySQL server side. It also contains test cases to demonstrate how current
timezone value of SimpleDateFormat and JVM's default timezone setting affects parsing and formatting of the date values.
Similar test cases exist for JacksonObjectMapper's JSON object serialization process as well.

MySQL's TIMESTAMP data type is the only type which is affected by the timezone conversion during data storage. Keep in mind
that timestamp values are eventually stored in UTC, however, when those values are received from, and are sent to the client 
side, they are converted using the MySQL server's session timezone value. Many of the data accuracy problems and confusions
arise because of this conversion.

The best way to work with timestamp data types is to use GMT/UTC timezone setting both on the application side, and on the 
MySQL server side. If this is not the case, especially because of the MySQL server's timezone setting, when it is other than 
the UTC, then you can still work with the same timezone across the application and MySQL, by setting the session timezone while
connecting to the MySQL server. This can be achievable by setting `connectionTimezone=UTC` (previously `serverTimezone`, 
but it is deprecated) and`forceConnectionTimeZoneToSession=true` JDBC url parameters together. That way, there will be no 
timezone conversion process during the data storage and retrieval phases. If your application operates with UTC, but you 
need to keep MySQL server's session timezone derived from the server/global timezone variables, then your application side 
has to respect this, and set the `connectionTimezone` parameter same as the MySQL server timezone. For example, if the
MySQL server timezone is America/Los_Angeles, then `connectionTimezone`/`serverTimezone` should be set so as well. Setting 
that parameter with a different value, usually if your application works in UTC, then as UTC, will cause timestamp values to be
wrongly processed by the MySQL server side.

### Why do we get errors like "Data truncation: Incorrect datetime value: '2022-03-13 02:01:00' for column 'ts_value' at row 1" ?

Actually, this error is totally because of a wrong configuration mentioned above. If the MySQL server operates with a 
daylight saving timezone, like America/Los_Angeles, when the application sends a timestamp value which corresponds to a 
non-existing time period for that DST timezone, such as 02:00-02:59 on 13th March 2022 for PDT, the MySQL server wouldn't 
be able to process this value on its side, and raises the error. The root cause for this problem is that application side 
doesn't serialize the timestamp value with the timezone of the MySQL server operates with, for example, it might serialize 
the value with GMT/UTC timezone, which causes no timezone conversion, and then MySQL server attempts to deserialize this 
value and tries to reconstruct it as a timestamp value on its side, however, as the value belongs to a non-existent time 
period in MySQL server's timezone, it fails during this step, and raises the error with its message 
"Data truncation: Incorrect datetime value".

In order to avoid such kind of errors, your application must employ the exact timezone corresponding to the MySQL server's 
timezone during this serialization process. Setting `connectionTimezone` or `serverTimezone` JDBC url parameters with a value
corresponding to the MySQL server's timezone will do this.

### How using a daylight saving timezone might cause loss of value accuracy?

When working with a daylight saving timezone, such as America/Los_Angeles, there are two time points every year at which
moving the clock either 1 hour backward or forward occurs, and if your application uses such a daylight saving timezone, its
timestamp values will get affected at those times. Loss of value accuracy occurs when the clock is moved 1 hour backward,
and clients which are working with a non-daylight saving timezone such as GMT/UTC, are sending timestamp values during that
1-hour period.

<img src="https://github.com/ksevindik/mysql-timezone-examples/blob/master/images/pdt_timezone_data_accuracy_issue.png" width="400" height="600" alt="pdt_timezone_data_accuracy_issue"/>

On the other hand, when clock is moved 1-hour forward, there happens no data accuracy loss, however, those timestamp values,
persisted during this period, might be a bit confusing if you examine the data with a daylight saving timezone, instead of GMT/UTC.

<img src="https://github.com/ksevindik/mysql-timezone-examples/blob/master/images/pdt_timezone_one_hour_ahead.png" width="400" height="600" alt="pdt_timezone_one_hour_ahead"/>

### What should we pay attention to while working with SimpleDateFormat in Java?

First of all, you should be aware of that SimpleDateFormat class is not thread safe, so you should not work with instance 
variables of it, but create local variables and work with them instead. SimpleDateFormat internally utilizes a Calendar
object which has its own Timezone property, and that timezone property gets into effect if it is set through SimpleDateFormat's 
setTimezone() method. If it is not set, then JVM's default Timezone setting will be employed during the Date parsing and
formatting operations. Usage of timezone property also depends on whether offset section in the format pattern is specified or not.
For example if your format pattern is like `"yyyy-MM-dd'T'HH:mm:ss.sss"` without any offset part in it, then timezone property
of the SimpleDateFormat will be used during the parsing and formatting operations.

```kotlin
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss")
        val dateString = "2022-08-23T07:00:00.000"

        MatcherAssert.assertThat(TimeZone.getDefault().id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("UTC"))
        val dateParsedWithUTC = dateFormat.parse(dateString)
        MatcherAssert.assertThat(dateParsedWithUTC.time,Matchers.equalTo(1661238000000))

        dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("America/Los_Angeles"))
        val dateParsedWithPDT = dateFormat.parse(dateString)
        MatcherAssert.assertThat(dateParsedWithPDT.time,Matchers.equalTo(1661263200000))
```

```kotlin
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss")
        val date1 = Date(1661238000000)
        val date2 = Date(1661263200000)
        
        MatcherAssert.assertThat(TimeZone.getDefault().id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.format(date1),Matchers.equalTo("2022-08-23T07:00:00.000"))
        MatcherAssert.assertThat(dateFormat.format(date2),Matchers.equalTo("2022-08-23T14:00:00.000"))
        
        dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.format(date1),Matchers.equalTo("2022-08-23T00:00:00.000"))
        MatcherAssert.assertThat(dateFormat.format(date2),Matchers.equalTo("2022-08-23T07:00:00.000"))
```

However, if your format pattern includes offset part, like `"yyyy-MM-dd'T'HH:mm:ss.sssZ"`, then timezone property won't 
involve during format and parse operations.

```kotlin
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ")
        val dateString = "2022-08-23T07:00:00.000+0000"

        MatcherAssert.assertThat(TimeZone.getDefault().id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("UTC"))
        val dateParsedWithUTC = dateFormat.parse(dateString)
        MatcherAssert.assertThat(dateParsedWithUTC.time,Matchers.equalTo(1661238000000))

        dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("America/Los_Angeles"))
        val dateParsedWithPDT = dateFormat.parse(dateString)
        MatcherAssert.assertThat(dateParsedWithPDT.time,Matchers.equalTo(1661238000000))
```

```kotlin
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sssZ")
        val date1 = Date(1661238000000)
        val date2 = Date(1661263200000)

        MatcherAssert.assertThat(TimeZone.getDefault().id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("UTC"))
        MatcherAssert.assertThat(dateFormat.format(date1),Matchers.equalTo("2022-08-23T07:00:00.000+0000"))
        MatcherAssert.assertThat(dateFormat.format(date2),Matchers.equalTo("2022-08-23T14:00:00.000+0000"))

        dateFormat.timeZone = TimeZone.getTimeZone(ZoneId.of("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.timeZone.id,Matchers.equalTo("America/Los_Angeles"))
        MatcherAssert.assertThat(dateFormat.format(date1),Matchers.equalTo("2022-08-23T00:00:00.000-0700"))
        MatcherAssert.assertThat(dateFormat.format(date2),Matchers.equalTo("2022-08-23T07:00:00.000-0700"))
```

### How to work with Java's new DateTimeFormatter API even when we are working with old Date API?

It is so easy to work with Java's new `DateTimeFormatter` class even your date values are still in old Date class. 
`java.util.Date` class contains `toInstant` method to convert current `Date` value to an `Instant` value, then you can
construct a `LocalDateTime` object by using this `Instant` value for a particular `Timezone`. After obtaining a
`LocalDateTime` object, you can either use its `format` method or the DateTimeFormatter's.

```kotlin
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val date = Date(1661238000000) //2022-08-23T07:00:00.000 in UTC
        val localDateInUTC = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"))
        val localDateInPDT = LocalDateTime.ofInstant(date.toInstant(),ZoneId.of("America/Los_Angeles"))
        Assertions.assertEquals("2022-08-23T07:00:00.000",formatter.format(localDateInUTC))
        Assertions.assertEquals("2022-08-23T00:00:00.000",localDateInPDT.format(formatter))
```

As you see from the above sample, DateTimeFormatter always make use of the Timezone value of the LocalDateTime object 
compared to SimpleDateFormat.

### What should we pay attention to while working with Jackson ObjectMapper in Java?

If you are working with Jackson `ObjectMapper`, especially within your Spring Boot enabled applications to obtain JSON 
serialized views of your Java objects, you should be aware of that ObjectMapper class make use of its own DateFormat 
property during the JSON serialization. By default, ObjectMapper has been assigned with a DateFormat object with its own 
Timezone property already set, therefore format operation is performed by utilizing this Timezone value, which is by default UTC.

```kotlin
        val date = Date(1661238000000) //2022-08-23T07:00:00.000 in UTC
        val testEntity = TestEntity()
        testEntity.tsValue = date

        val actualJson = objectMapper.writeValueAsString(testEntity)
        val expectedJson = """{"id":null,"tsValue":"2022-08-23T07:00:00.000+00:00","dtValue":null}""".trimIndent()
        Assertions.assertEquals(expectedJson,actualJson)


        TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"))
        
        val actualJson2 = objectMapper.writeValueAsString(testEntity)
        val expectedJson2 = """{"id":null,"tsValue":"2022-08-23T07:00:00.000+00:00","dtValue":null}""".trimIndent()
        Assertions.assertEquals(expectedJson2,actualJson2)
```

If you want to change this Timezone property, you can easily achieve this via setting 
`spring.jackson.timeZone` property, such as `spring.jackson.timeZone=America/Los_Angeles`.

```kotlin
        val date = Date(1661238000000) //2022-08-23T07:00:00.000 in UTC
        val testEntity = TestEntity()
        testEntity.tsValue = date

        val actualJson = objectMapper.writeValueAsString(testEntity)
        val expectedJson = """{"id":null,"tsValue":"2022-08-23T00:00:00.000-07:00","dtValue":null}""".trimIndent()
        Assertions.assertEquals(expectedJson,actualJson)


        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        val actualJson2 = objectMapper.writeValueAsString(testEntity)
        val expectedJson2 = """{"id":null,"tsValue":"2022-08-23T00:00:00.000-07:00","dtValue":null}""".trimIndent()
        Assertions.assertEquals(expectedJson2,actualJson2)
```

### What should we pay attention to while working with Google's Protobuf Timestamp?

According to Timestamp's Javadoc comment;

>"A Timestamp represents a point in time independent of any time zone or local
calendar, encoded as a count of seconds and fractions of seconds at
nanosecond resolution. The count is relative to an epoch at UTC midnight on
January 1, 1970, in the proleptic Gregorian calendar which extends the
Gregorian calendar backwards to year one."

Therefore, there is no Timezone information involved while working with Protobuf Timestamp values.