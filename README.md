# 1Source-Simulator

A simulator for EquiLend's 1Source Digital Ledger. It can act as a borrower or lender by automatically responding to 1Source loan lifecycle events.

## Description

The 1Source Simulator is an auto-responder — a rules-driven, configurable program — which can be configured to behave like a counterparty on the 1Source ledger to facilitate development and testing. The Simulator reads in configuration files which contain rules and other information to determine how it will operate and behave.

Possible configurable options include:

- acting on existing loans
- responding immediately or with an optional delay to live incoming events
- generating periodic contract proposals

The Simulator is also configured to act as one party on the loan, either the borrower or lender. This versatility facilitates comprehensive testing.

## Getting Started

### Dependencies

- The 1Source-Simulator requires Java JDK 17 or later
- Apache Maven 3.8.0 or later

### Installing

Clone the code repository locally from GitHub with the following command:

```
> git clone https://github.com/equilend/1Source-Simulator
> cd 1Source-Simulator
```

### Compling and Packaging:

```
1Source-Simulator> mvn clean package
```

### Running the Simulator

To execute the Simulator using Maven:

```
1Source-Simulator> mvn exec:java -Dexec.mainClass="com.equilend.simulator.Simulator"
```

To execute the Simulator using from a Bash shell script:

```
1Source-Simulator> ./run.sh
```

### Stopping the Simulator

To shut down the Simulator:

```
1Source-Simulator> <Ctrl-C>
```
