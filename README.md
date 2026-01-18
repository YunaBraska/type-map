# TypeMap & TypeList incl. TypeConverter

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

## Installation

### Maven
```xml
<dependency>
  <groupId>berlin.yuna</groupId>
  <artifactId>type-map</artifactId>
  <version>${type-map.version}</version>
</dependency>
```

### Gradle
```gradle
implementation("berlin.yuna:type-map:${typeMapVersion}")
```

## Introduction

Greetings, Java developer. Are you tired of wrangling primitive type conversions, fighting rogue JSON strings, and
sacrificing your sanity to reflection-heavy libraries? Good news: the TypeMap/TypeList library exists to end this chaos.
Designed with performance, simplicity, and GraalVM native compatibility in mind, it transforms the art of type
management from a dreaded chore to a seamless experience.

## Motivation

Most libraries promise ease, but under the hood, they hide performance-draining reflection or endless dependencies. Not
this one. This library was forged from the frustration of clunky, bloated tools.

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
    - [Type](src/main/java/berlin/yuna/typemap/model/Type.java)
    - [TypeSet](src/main/java/berlin/yuna/typemap/model/TypeSet.java)
        - [ConcurrentTypeSet](src/main/java/berlin/yuna/typemap/model/ConcurrentTypeSet.java)
    - [TypeList](src/main/java/berlin/yuna/typemap/model/TypeList.java)
        - [ConcurrentTypeList](src/main/java/berlin/yuna/typemap/model/ConcurrentTypeList.java)
    - [TypeMap](src/main/java/berlin/yuna/typemap/model/TypeMap.java)
        - [LinkedTypeMap](src/main/java/berlin/yuna/typemap/model/LinkedTypeMap.java)
        - [ConcurrentTypeMap](src/main/java/berlin/yuna/typemap/model/ConcurrentTypeMap.java)
- Supportive Tools:
    - [TypeConverter](src/main/java/berlin/yuna/typemap/logic/TypeConverter.java)
    - [JsonEncoder](src/main/java/berlin/yuna/typemap/logic/JsonEncoder.java)
    - [JsonDecoder](src/main/java/berlin/yuna/typemap/logic/JsonDecoder.java)
    - [XmlEncoder](src/main/java/berlin/yuna/typemap/logic/XmlEncoder.java)
    - [XmlDecoder](src/main/java/berlin/yuna/typemap/logic/XmlDecoder.java)
    - [ArgsDecoder](src/main/java/berlin/yuna/typemap/logic/ArgsDecoder.java)
- Extension Mechanism:
    - [TypeConversionRegister](#register-custom-conversions)

### Getting Started

Quick start (JSON or XML input):
```java
TypeMap map = TypeMap.mapOf(jsonOrXml);
TypeList list = TypeList.listOf(jsonOrXml);
```

#### Basics
```java
TypeMap typeMap = new TypeMap().putR("myTime", new Date());
OffsetDateTime timeValue = typeMap.asOffsetDateTime("myTime");
OffsetDateTime timeValue2 = typeMap.as(OffsetDateTime.class, "myTime");
```

```java
TypeList typeList = new TypeList().addR(new Date());
OffsetDateTime timeValue = typeList.asOffsetDateTime(0);
OffsetDateTime timeValue2 = typeList.as(OffsetDateTime.class, 0);
```

#### Collections

```java
TypeMap typeMap = new TypeMap().putR("myKey", new String[]{"1", "2", "3"});

TypeList list1 = typeMap.asList("myKey");
Integer numberThree = typeMap.asList("myKey").as(2, Integer.class);
List<Integer> list2 = typeMap.asList(Integer.class, "myKey");
List<Integer> list3 = typeMap.asList(ArrayList::new, Integer.class, "myKey");
```

```java
TypeList typeList = new TypeList().addR(new String[]{"1", "2", "3"});

TypeList list1 = typeList.asList(0);
Integer numberThree = typeList.asList(0).as(2, Integer.class);
List<Integer> list2 = typeList.asList(Integer.class, 0);
List<Integer> list3 = typeList.asList(ArrayList::new, Integer.class, 0);
```

#### Maps

```java
TypeMap typeMap = new TypeMap().putR("myKey", Map.of(6, new Date()));

LinkedTypeMap map1 = typeMap.asMap("myKey");
Map<Long, Instant> map2 = typeMap.asMap(Long.class, Instant.class, "myKey");
Map<Long, Instant> map3 = typeMap.asMap(HashMap::new, Long.class, Instant.class, "myKey");
```

```java
TypeList typeList = new TypeList().addR(Map.of(6, new Date()));

LinkedTypeMap map1 = typeList.asMap(0);
Map<Long, Instant> map2 = typeList.asMap(Long.class, Instant.class, 0);
Map<Long, Instant> map3 = typeList.asMap(HashMap::new, Long.class, Instant.class, 0);
```

#### JSON & XML

_TypeMap/TypeList use JsonDecoder/XmlDecoder internally; the decoder APIs are meant for advanced use._

```java
String jsonString = "{"
    + "\"outerMap\":{"
    + "\"innerMap\":{\"timestamp\":1800000000000},"
    + "\"myList\":[\"BB\",1,true,null,1.2]"
    + "}"
    + "}";

TypeMap jsonMap = TypeMap.mapOf(jsonString);
LinkedTypeMap inner = jsonMap.asMap("outerMap", "innerMap");
List<Object> list = jsonMap.asList(Object.class, "outerMap", "myList");
TestEnum enumValue = jsonMap.as(TestEnum.class, "outerMap", "myList", 0);
Long timestamp = jsonMap.asLong("outerMap", "innerMap", "timestamp");
String backToJson = jsonMap.toJson();
```

```java
String xmlString = "<root><id>7</id></root>";
TypeMap xmlMap = TypeMap.mapOf(xmlString);
Long id = xmlMap.asLong("id");
```

#### Streaming JSON (IO-backed streams must be closed)

```java
try (Stream<Pair<Integer, Object>> stream = JsonDecoder.streamJsonArray(path)) {
    stream.forEach(pair -> System.out.println(pair.value()));
}
```

#### Args

_ArgsDecoder is used internally_

```java
String[] cliArgs = {"myCommand1", "myCommand2", "--help", "-v2=true", "-param", "42"};
TypeMap map = new TypeMap(cliArgs);

Boolean help = map.asBoolean("help");
Boolean v2 = map.asBoolean("v2");
List<Integer> paramList = map.asList(Integer.class, "param");
```

### TypeConverter

_TypeConverter is the core of TypeMap/TypeList conversions_

* `TypeConverter.mapOf`
* `TypeConverter.convertObj`
* `TypeConverter.collectionOf`
* `JsonEncoder.toJson`
* `JsonDecoder.mapOf`
* `JsonDecoder.listOf`
* `JsonDecoder.typeOf`

```java
// ENUM
TestEnum enumValue = enumOf("BB", TestEnum.class);

// OBJECT
TestEnum enumValue2 = convertObj("BB", TestEnum.class);
Long longValue = convertObj("1800000000000", Long.class);
OffsetDateTime timeValue = convertObj("1800000000000", OffsetDateTime.class);

// MAP
TypeMap map1 = mapOf(Map.of("12", "34", "56", "78"));
Map<String, Integer> map2 = mapOf(Map.of("12", "34", "56", "78"), String.class, Integer.class);
Map<String, Integer> map3 = mapOf(Map.of("12", "34", "56", "78"), HashMap::new, String.class, Integer.class);

// COLLECTION
TypeList list1 = collectionOf(Arrays.asList("123", "456"));
Set<Integer> list2 = collectionOf(Arrays.asList("123", "456"), Integer.class);
Set<Integer> set1 = collectionOf(Arrays.asList("123", "456"), HashSet::new, Integer.class);
```

### Register custom conversions

_Exceptions are ignored and result in `null`_

```java
conversionFrom(String.class).to(Integer.class).register(Path::toUri);
TypeConversionRegister.registerTypeConvert(Path.class, File.class, Path::toFile);
TypeConversionRegister.registerTypeConvert(Path.class, URL.class, path -> path.toUri().toURL());
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
[java_version]: https://img.shields.io/badge/java-17-blueviolet?style=flat-square
