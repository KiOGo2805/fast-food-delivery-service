# JaCoCo Configuration Examples

Reference material for the Unit Testing lecture. Each block below is a
**standalone example** of one JaCoCo configuration option - not meant to
all be pasted in at once. Our actual `pom.xml` uses a version close to
Example 1 (basic) + Example 2 (exclusions).

All examples assume the same base plugin declaration:

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <executions>
        <!-- goals go here - see each example below -->
    </executions>
</plugin>
```

---

## Example 1 — Basic: instrument + report, no enforcement

The simplest useful setup. Generates a report; doesn't fail the build if
coverage is low.

```xml
<executions>
    <execution>
        <id>prepare-agent</id>
        <goals>
            <goal>prepare-agent</goal>
        </goals>
    </execution>
    <execution>
        <id>report</id>
        <phase>test</phase>
        <goals>
            <goal>report</goal>
        </goals>
    </execution>
</executions>
```

Run `mvn test`, open `target/site/jacoco/index.html`. That's it - purely
informational, nothing stops a low-coverage build from succeeding.

---

## Example 2 — Excluding packages from coverage entirely

Useful when some code is deliberately out of scope for unit tests (e.g.
security config that needs a real Keycloak/JWT setup, or a plain `main()`
entry point). Excluded classes disappear from the report AND from any
`check` calculation - so use this deliberately, not to hide inconvenient
numbers.

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <configuration>
        <excludes>
            <exclude>com/java/carparts/configuration/**</exclude>
            <exclude>com/java/carparts/CarPartsStoreApplication.class</exclude>
            <exclude>**/*Application.class</exclude>
        </excludes>
    </configuration>
    <executions>
        <execution>
            <id>prepare-agent</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## Example 3 — Enforced whole-project gate (what we actually use)

Fails `mvn verify` if overall instruction coverage drops below the
threshold. `<element>BUNDLE</element>` means "the whole project," treated
as one number.

```xml
<execution>
    <id>check</id>
    <phase>verify</phase>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>INSTRUCTION</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.60</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

---

## Example 4 — Per-package gate (stricter on code that matters most)

Instead of one project-wide number, require a higher bar specifically on
`service` (business logic) while leaving other packages unchecked. Multiple
`<rule>` blocks can coexist.

```xml

<execution>
    <id>check</id>
    <phase>verify</phase>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>PACKAGE</element>
                <includes>
                    <include>com.java.fastfood.service</include>
                </includes>
                <limits>
                    <limit>
                        <counter>INSTRUCTION</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
            <rule>
                <element>PACKAGE</element>
                <includes>
                    <include>com.java.fastfood.controller</include>
                </includes>
                <limits>
                    <limit>
                        <counter>INSTRUCTION</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.60</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

---

## Example 5 — Branch coverage, not just instructions

Instruction coverage can look deceptively high if only one side of an
`if`/`else` was ever exercised. Adding a `BRANCH` limit alongside
`INSTRUCTION` catches that.

```xml
<execution>
    <id>check</id>
    <phase>verify</phase>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>INSTRUCTION</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.60</minimum>
                    </limit>
                    <limit>
                        <counter>BRANCH</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.50</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

---

## Example 6 — Per-class exclusion inside an otherwise-checked package

Sometimes you want most of a package covered, but one specific class (e.g.
a config/constants holder) exempted. `<excludes>` at the plugin
`<configuration>` level (not inside a `<rule>`) applies globally, including
to `check`.

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <configuration>
        <excludes>
            <exclude>com/java/carparts/domain/dto/**</exclude>
            <!-- DTOs are plain data holders - excluding them from the
                 gate avoids "coverage" that's really just Lombok
                 getter/setter noise from other tests constructing them -->
        </excludes>
    </configuration>
    ...
</plugin>
```

---

## Example 7 — Non-blocking check (warn, don't fail)

Same rule as Example 3, but `haltOnFailure` set to `false` - prints a
warning in the build log instead of failing `mvn verify`. Useful if you
want visibility on a coverage trend without gating merges on it yet (e.g.
early in a project before the team has a testing habit established).

```xml
<execution>
    <id>check</id>
    <phase>verify</phase>
    <goals>
        <goal>check</goal>
    </goals>
    <configuration>
        <haltOnFailure>false</haltOnFailure>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>INSTRUCTION</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.60</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</execution>
```

---

## Example 8 — Multiple report output formats (for CI tooling)

The default `report` goal produces HTML only. Some CI tools (SonarQube,
Codecov, GitHub coverage badges) want XML or CSV instead/as well.

```xml
<execution>
    <id>report</id>
    <phase>test</phase>
    <goals>
        <goal>report</goal>
    </goals>
    <configuration>
        <formats>
            <format>HTML</format>
            <format>XML</format>
        </formats>
    </configuration>
</execution>
```

---

## Example 9 — Merging coverage from multiple test runs

Relevant later in the semester once integration tests (Failsafe/
Testcontainers) run separately from unit tests (Surefire) and write to a
different `.exec` file - `merge` combines them into one report instead of
two competing partial ones.

```xml
<execution>
    <id>merge-results</id>
    <phase>verify</phase>
    <goals>
        <goal>merge</goal>
    </goals>
    <configuration>
        <fileSets>
            <fileSet>
                <directory>${project.build.directory}</directory>
                <includes>
                    <include>*.exec</include>
                </includes>
            </fileSet>
        </fileSets>
        <destFile>${project.build.directory}/merged.exec</destFile>
    </configuration>
</execution>
```

---

## Quick reference: `<element>` values for rules

| Element | Scope |
|---|---|
| `BUNDLE` | Whole project (one combined number) |
| `PACKAGE` | One or more packages, via `<includes>`/`<excludes>` |
| `CLASS` | Individual classes |
| `SOURCEFILE` | Individual source files |
| `METHOD` | Individual methods (very granular, rarely used at gate-level) |

## Quick reference: `<counter>` values

| Counter | Measures |
|---|---|
| `INSTRUCTION` | Bytecode instructions (finest, hardest to fake) |
| `BRANCH` | `if`/`switch`/ternary paths |
| `LINE` | Source lines touched |
| `COMPLEXITY` | Cyclomatic complexity covered |
| `METHOD` | Methods invoked at all |
| `CLASS` | Classes touched at all |
