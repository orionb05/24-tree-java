# 🌳 2–4 Tree Implementation in Java

This project is a complete **self-balancing 2–4 tree** (also called a 2-3-4 tree), written from scratch in Java. It supports **insertion**, **search**, **deletion**, and **in-order printing**, and handles internal rebalancing through splitting, merging, and rotating nodes.

> 🛠️ Developed as a course project, this tree uses integer values and includes logic for node classification (2-node, 3-node, 4-node), dynamic restructuring, and subtree manipulation — all without relying on built-in tree libraries.

---

## 📌 Features

- ✅ Full support for:
  - `addValue(int)` — insert integers
  - `hasValue(int)` — search for values
  - `deleteValue(int)` — remove values with rebalancing
- 🌲 Distinguishes between 2-node, 3-node, and 4-node structures
- 🔄 Handles:
  - Node **splitting**
  - Sibling **rotation**
  - Node **merging**
- 📤 In-order tree printing (`printInOrder()`)
- 🧪 Debug mode (prints trace output when enabled)

---

## 🚀 How to Use

### 🔧 Requirements
- Java 8 or higher

### ▶️ Run Test File

```bash
A test file is included to demonstrate usage of the `TwoFourTree` class.
