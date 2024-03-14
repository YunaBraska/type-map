# TypeMap & TypeList incl. TypeConverter & JsonConverter

[![Build][build_shield]][build_link]
[![Maintainable][maintainable_shield]][maintainable_link]
[![Coverage][coverage_shield]][coverage_link]
[![Issues][issues_shield]][issues_link]
[![Commit][commit_shield]][commit_link]
[![Dependencies][dependency_shield]][dependency_link]
[![License][license_shield]][license_link]
[![Central][central_shield]][central_link]
[![Tag][tag_shield]][tag_link]
[![Javadoc][javadoc_shield]][javadoc_link]
[![Size][size_shield]][size_shield]
![Label][label_shield]
![Label][java_version]

## Introduction

In modern Java development, managing data types efficiently and effectively can be a complex task, especially when
dealing with dynamic data sources or preparing for GraalVM native compilation. Our TypeMap & TypeList library, equipped
with the powerful TypeConverter and JsonConverter, is engineered to simplify these challenges. It's designed for
developers who seek a hassle-free way to handle diverse data types and conversions, enabling you to focus more on
business logic rather than boilerplate code.

## Motivation

Working with type conversions in Java, I frequently encountered libraries that were either heavy on reflection, consumed
substantial memory, or were cumbersome to extend. This inspired me to create a solution that's lightweight, easy to
understand, and simple to enhance, all without any "magic" under the hood.

### Benefits

- **Pure Java**: No external dependencies for a lightweight integration.
- **Functional Design**: Embraces modern Java functional paradigms.
- **No Reflection**: Ensures compatibility with GraalVM native images.
- **High Performance**: Tailored for efficiency in intensive workloads.
- **Dynamic & Extendable**: Simplifies adding new type conversions.
- **Type Safety**: Utilizes Java generics for compile-time type checking.
- **Lazy Loading**: Conversions occur only when needed, enhancing performance and reducing memory usage.

### Classes

- Core Components:
    - [TypeList](#basics)
    - [TypeMap](#basics)
- Supportive Tools:
    - [TypeConverter](#typeconverter)
    - [JsonEncoder](#json)
    - [JsonDecoder](#json)
    - [ArgsDecoder](#args)
- Extension Mechanism:
    - [TypeConversionRegister](#register-custom-conversions)

### Usage

#### Basics

- _[TypeMap](src/main/java/berlin/yuna/typemap/model/TypeMap.java)_
- _[LinkedTypeMap](src/main/java/berlin/yuna/typemap/model/LinkedTypeMap.java)_
- _[ConcurrentTypeMap](src/main/java/berlin/yuna/typemap/model/ConcurrentTypeMap.java)_
- _[TypeList](src/main/java/berlin/yuna/typemap/model/TypeList.java)_
- _[ConcurrentTypeList](src/main/java/berlin/yuna/typemap/model/ConcurrentTypeList.java)_
- _[TypeSet](src/main/java/berlin/yuna/typemap/model/TypeSet.java)_
- _[ConcurrentTypeSet](src/main/java/berlin/yuna/typemap/model/ConcurrentTypeSet.java)_

```java
    TypeMap typeMap=new TypeMap().putt(new Date(), "myTime");
    OffsetDateTime timeValue=typeMap.get(OffsetDateTime.class, "myTime");
```

```java
    TypeList typeList=new TypeList().addd(new Date());
    String timeValue=typeList.get(OffsetDateTime.class, 0);
```

#### Collections

```java
    TypeMap typeMap=new TypeMap().putt("myKey",new String[]{"1","2","3"});

    TypeList list1=typeMap.getList("myKey");
    Integer numberThree=typeList.getList("myKey").get(2,Integer.class)
    List<Integer> list2=typeMap.getList(Integer.class, "myKey");
    List<Integer> list3=typeMap.getList(ArrayList::new,Integer.class, "myKey");
```

```java
    TypeList typeList=new TypeList().addd(new String[]{"1","2","3"});

    TypeList list1=typeList.getList(0);
    Integer numberThree=typeList.getList(0).get(Integer.class, 2)
    List<Integer> list2=typeList.getList(Integer.class, 0);
    List<Integer> list3=typeList.get(Integer::new,Integer.class, 0);
```

#### Maps

```java
    TypeMap typeMap=new TypeMap().putt("mykey",Map.of(6,new Date()));

LinkedTypeMap map1 = typeMap.getMap("myKey");
Map<Long, Instant> map2 = typeMap.getMap(Long.class, Instant.class, "mykey")
Map<Long, Instant> map2 = typeMap.getMap(() -> new HashMap<>(), Long.class, "mykey")
```

```java
    TypeList typeLst = new TypeList().addd(Map.of(6, new Date()));

LinkedTypeMap map1 = typeLst.getMap(0);
Map<Long, Instant> map2 = typeLst.getMap(Long.class, Instant.class, 0);
Map<Long, Instant> map3 = typeLst.getMap(HashMap::new, Long.class, Instant.class, 0);
```

#### Json

_[JsonEncoder](src/main/java/berlin/yuna/typemap/logic/JsonEncoder.java) & [JsonDecoder](src/main/java/berlin/yuna/typemap/logic/JsonDecoder.java)
is used internally_

```java
    final String jsonString =
    "{\n"
        + "  \"outerMap\": {\n"
        + "    \"innerMap\": {\n"
        + "      \"timestamp\": 1800000000000,\n"
        + "    },\n"
        + "    \"myList\": [\"BB\",1,true,null,1.2]\n"
        + "  }\n"
        + "}";
final TypeMap jsonMap = new TypeMap(jsonString);

final LinkedTypeMap map1 = jsonMap.getMap("outerMap", "innerMap")
final Map<String, Instant> map2 jsonMap.getMap(String .class, Instant .class,"outerMap","innerMap")

final TypeList list1 = jsonMap.getMap("outerMap").getList("myList")
final TypeList list2 = jsonMap.getList("outerMap", "myList")
final List<Object> list3 = jsonMap.getList(Object.class, "outerMap", "myList")
final TestEnum enum1 = jsonMap.getMap("outerMap").getList("myList").get(TestEnum.class, 0)
final TestEnum enum2 = jsonMap.getList("outerMap", "myList").get(TestEnum.class, 0)

final Date myDate = jsonMap.get(Date.class, "outerMap", "innerMap", "timestamp");
final Long myTimestamp = jsonMap.get(Long.class, "outerMap", "innerMap", "timestamp");
final TestEnum myEnum = jsonMap.get(TestEnum.class, "outerMap", "myList", 0);
final Boolean myBoolean = jsonMap.get(Boolean.class, "outerMap", "myList", 2);
final String backToJson = jsonMap.toJson();
```

#### Args

_[ArgsDecoder](src/main/java/berlin/yuna/typemap/logic/ArgsDecoder.java) is used internally_

```java
final String[] cliArgs = {" myCommand1    myCommand2 --help  -v2=\"true\" -v=\"true\" -v=\"true\" --verbose=\"true\"   -Args=\"true\" -param 42   54   -ArgList=\"item 1\" --ArgList=\"item 2\" -v2=\"false\" --ArgList=\"-item 3\"  "};
final TypeMap map1 = new TypeMap(cliArgs);

final Boolean help = map.get(Boolean.class, "help");
final Boolean v = map.get(Boolean.class, "v");
final Boolean v2 = map.get(Boolean.class, "v2");
final Boolean verbose = map.get(Boolean.class, "verbose");
final Boolean args = map.get(Boolean.class, "Args");
final List<Boolean> v2List = map.getList("v2", Boolean.class)
final List<Integer> paramList = map.getList("param", Integer.class);
final TypeList argList = map.getList("ArgList");
```

### TypeConverter

_[TypeConverter](src/main/java/berlin/yuna/typemap/logic/TypeConverter.java) - The `TypeConverter` is the core of
the `TypeMap`/`TypeList`_

* `TypeConverter.mapOf`
* `TypeConverter.convertObj`
* `TypeConverter.collectionOf`
* `JsonEncoder.toJson`
* `JsonDecoder.jsonOf`
* `JsonDecoder.jsonMapOf`
* `JsonDecoder.jsonListOf`

```java

// ENUM
final TestEnum enum2=enumOf("BB",TestEnum.class)

// OBJECT
final TestEnum enum1=convertObj("BB",TestEnum.class)
final Long long1=convertObj("1800000000000",Long.class)
final OffsetDateTime time1=convertObj("1800000000000",OffsetDateTime.class)

// MAP
final TypeMap map1=mapOf(Map.of("12","34","56","78"))
final Map<String, Integer> map2=mapOf(Map.of("12","34","56","78"),String.class,Integer.class)
final Map<String, Integer> map3=mapOf(Map.of("12","34","56","78"),()->new HashMap<>(),String.class,Integer.class)

// COLLECTION
final TypeList list1=collectionOf(asList("123","456"))
final Set<Integer.class>list2=collectionOf(asList("123","456"),Integer.class)
final Set<Integer.class>set1=collectionOf(asList("123","456"),()->new HashSet<>(),Integer.class)
```

### Extension Mechanism

### Register custom conversions

_[TypeConversionRegister](src/main/java/berlin/yuna/typemap/config/TypeConversionRegister.java) - Exception are ignored
and results in `null`_

```java
    conversionFrom(String.class).to(Integer.class).register(Path::toUri);
    TypeConversionRegister.registerTypeConvert(Path.class,File.class,Path::toFile);
    TypeConversionRegister.registerTypeConvert(Path.class,URL.class,path->path.toUri().toURL());
```

[build_shield]: https://github.com/YunaBraska/type-map/workflows/MVN_RELEASE/badge.svg

[build_link]: https://github.com/YunaBraska/type-map/actions?query=workflow%3AMVN_RELEASE

[maintainable_shield]: https://img.shields.io/codeclimate/maintainability/YunaBraska/type-map?style=flat-square

[maintainable_link]: https://codeclimate.com/github/YunaBraska/type-map/maintainability

[coverage_shield]: https://img.shields.io/codeclimate/coverage/YunaBraska/type-map?style=flat-square

[coverage_link]: https://codeclimate.com/github/YunaBraska/type-map/test_coverage

[issues_shield]: https://img.shields.io/github/issues/YunaBraska/type-map?style=flat-square

[issues_link]: https://github.com/YunaBraska/type-map/commits/main

[commit_shield]: https://img.shields.io/github/last-commit/YunaBraska/type-map?style=flat-square

[commit_link]: https://github.com/YunaBraska/type-map/issues

[license_shield]: https://img.shields.io/github/license/YunaBraska/type-map?style=flat-square

[license_link]: https://github.com/YunaBraska/type-map/blob/main/LICENSE

[dependency_shield]: https://img.shields.io/librariesio/github/YunaBraska/type-map?style=flat-square

[dependency_link]: https://libraries.io/github/YunaBraska/type-map

[central_shield]: https://img.shields.io/maven-central/v/berlin.yuna/type-map?style=flat-square

[central_link]:https://search.maven.org/artifact/berlin.yuna/type-map

[tag_shield]: https://img.shields.io/github/v/tag/YunaBraska/type-map?style=flat-square

[tag_link]: https://github.com/YunaBraska/type-map/releases

[javadoc_shield]: https://javadoc.io/badge2/berlin.yuna/type-map/javadoc.svg?style=flat-square

[javadoc_link]: https://javadoc.io/doc/berlin.yuna/type-map

[size_shield]: https://img.shields.io/github/repo-size/YunaBraska/type-map?style=flat-square

[label_shield]: https://img.shields.io/badge/Yuna-QueenInside-blueviolet?style=flat-square

[gitter_shield]: https://img.shields.io/gitter/room/YunaBraska/type-map?style=flat-square

[gitter_link]: https://gitter.im/type-map/Lobby

[java_version]: https://img.shields.io/badge/java-8-blueviolet?style=flat-square
