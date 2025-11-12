# CLAUDE.md - Community Standard Repository

This file provides guidance to Claude Code (claude.ai/code) when working with standard content in this repository.

## Project Overview

This is the **ZeroBias Community Standard Repository** containing open-source compliance standards (frameworks and benchmarks). Standards define formal documents with referenceable elements like requirements, test cases, and technical specifications.

**Repository Role:** Community-contributed compliance frameworks, benchmarks, and formal standards

This repository follows the same structure as `auditlogic/standard` but contains community-contributed, open-source standards.

## Current Status

⚠️ **AI-Assisted Development Workflows Needed**

This CLAUDE.md is a placeholder. Comprehensive AI-assisted development workflows for creating and maintaining standards are planned but not yet implemented.

**What's Needed:**
- Step-by-step workflows for creating new standards
- Element structuring and organization
- Validation procedures
- Publishing and versioning guidelines
- Framework vs benchmark distinction
- Integration with crosswalks

## Repository Structure

```
standard/
├── package/zerobias/          # Community standard packages
│   └── <standard-name>/       # Individual standard
│       ├── package.json       # NPM package configuration
│       ├── index.yml          # Standard metadata
│       ├── elements.yml       # Requirements/test cases/elements
│       ├── CHANGELOG.md       # Version history
│       └── npm-shrinkwrap.json
├── scripts/                   # Creation and validation scripts
├── lerna.json                 # Monorepo configuration
└── README.md
```

## File Format Reference

**Source of Truth:** `../../auditmation/platform/dataloader/src/processors/standard/`

**Expected Structure:**
- `index.yml` - Standard metadata (name, version, type, description)
- `elements.yml` - Elements (requirements for frameworks, test cases for benchmarks)
- `package.json` - Must include `auditmation.import-artifact: "standard"`

## Standard Types

### Framework
Non-prescriptive requirements defining WHAT must be done:
- NIST Cybersecurity Framework
- ISO 27001
- SOC 2
- HIPAA
- PCI DSS

### Benchmark
Prescriptive test cases defining HOW to achieve compliance:
- CIS Benchmarks
- DISA STIGs
- Custom security baselines

### Other Standards
Formal documents with referenceable elements:
- Technical specifications
- Policies and procedures
- Industry guidelines

## Integration with Platform

### Dataloader Integration
**Handler Location:** `../../auditmation/platform/dataloader/src/processors/standard/`
**Database Tables:**
- `catalog.standard` - Standard metadata
- `catalog.element` - Individual requirements/test cases

### Usage in Platform
- **Audit Framework Selection:** Choose which standards apply to audit
- **Requirement Mapping:** Link evidence to standard elements
- **Crosswalks:** Map requirements between standards
- **Gap Analysis:** Identify unmapped requirements

## Related Documentation

- **[Root CLAUDE.md](../../CLAUDE.md)** - Meta-repo guidance
- **[ContentArtifacts.md](../../ContentArtifacts.md)** - Content catalog system
- **[auditlogic/standard/CLAUDE.md](../../auditlogic/standard/CLAUDE.md)** - Proprietary standards (same pattern)
- **[auditmation/platform/dataloader/CLAUDE.md](../../auditmation/platform/dataloader/CLAUDE.md)** - Dataloader processor
- **[zerobias-org/crosswalk/CLAUDE.md](../crosswalk/CLAUDE.md)** - Framework mappings
- **[zerobias-org/framework/CLAUDE.md](../framework/CLAUDE.md)** - Community frameworks

## Important Notes

### Community vs Proprietary

**This Repository (zerobias-org/standard):**
- Open-source, community-contributed standards
- Public GitHub repository
- MIT/Apache license
- Community validation

**Proprietary Repository (auditlogic/standard):**
- Closed-source, professionally validated standards
- Private GitHub repository
- Commercial license
- Expert review

Both follow identical structure and use same dataloader processor.

## Future Development

Once AI-assisted development workflows are implemented, this CLAUDE.md will include:
- Creating new framework from template
- Defining requirements and elements
- Validation and testing procedures
- Publishing to NPM registry
- Updating standards when regulations change
- Integration with crosswalks and benchmarks

---

**Last Updated:** 2025-11-11
**Maintainers:** ZeroBias Community

