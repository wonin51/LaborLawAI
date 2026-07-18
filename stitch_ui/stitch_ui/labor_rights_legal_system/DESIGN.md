---
name: Labor Rights Legal System
colors:
  surface: '#f7f9ff'
  surface-dim: '#cfdbea'
  surface-bright: '#f7f9ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#ecf4ff'
  surface-container: '#e3effe'
  surface-container-high: '#ddeaf8'
  surface-container-highest: '#d7e4f2'
  on-surface: '#111d27'
  on-surface-variant: '#43474d'
  inverse-surface: '#26323d'
  inverse-on-surface: '#e7f2ff'
  outline: '#74777e'
  outline-variant: '#c3c6ce'
  surface-tint: '#47607e'
  primary: '#001d36'
  on-primary: '#ffffff'
  primary-container: '#17324d'
  on-primary-container: '#819aba'
  inverse-primary: '#afc9ea'
  secondary: '#146966'
  on-secondary: '#ffffff'
  secondary-container: '#a5f0eb'
  on-secondary-container: '#1e6f6c'
  tertiary: '#2a1700'
  on-tertiary: '#ffffff'
  tertiary-container: '#472a00'
  on-tertiary-container: '#ce8a28'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#d1e4ff'
  primary-fixed-dim: '#afc9ea'
  on-primary-fixed: '#001d36'
  on-primary-fixed-variant: '#2f4865'
  secondary-fixed: '#a5f0eb'
  secondary-fixed-dim: '#89d3cf'
  on-secondary-fixed: '#00201f'
  on-secondary-fixed-variant: '#00504d'
  tertiary-fixed: '#ffddb7'
  tertiary-fixed-dim: '#ffb95e'
  on-tertiary-fixed: '#2a1700'
  on-tertiary-fixed-variant: '#653e00'
  background: '#f7f9ff'
  on-background: '#111d27'
  surface-variant: '#d7e4f2'
typography:
  display-lg:
    fontFamily: Noto Serif SC
    fontSize: 40px
    fontWeight: '700'
    lineHeight: '1.2'
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Noto Serif SC
    fontSize: 32px
    fontWeight: '700'
    lineHeight: '1.2'
  headline-md:
    fontFamily: Noto Serif SC
    fontSize: 28px
    fontWeight: '600'
    lineHeight: '1.4'
  headline-sm:
    fontFamily: Noto Serif SC
    fontSize: 22px
    fontWeight: '600'
    lineHeight: '1.4'
  body-lg:
    fontFamily: Noto Sans SC
    fontSize: 18px
    fontWeight: '400'
    lineHeight: '1.6'
  body-md:
    fontFamily: Noto Sans SC
    fontSize: 16px
    fontWeight: '400'
    lineHeight: '1.6'
  body-sm:
    fontFamily: Noto Sans SC
    fontSize: 14px
    fontWeight: '400'
    lineHeight: '1.5'
  label-mono:
    fontFamily: JetBrains Mono
    fontSize: 13px
    fontWeight: '500'
    lineHeight: '1.0'
    letterSpacing: 0.05em
  caption:
    fontFamily: Noto Sans SC
    fontSize: 12px
    fontWeight: '500'
    lineHeight: '1.4'
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
  3xl: 64px
  gutter: 24px
  margin-mobile: 16px
  margin-desktop: auto
  max-width: 1200px
---

## Brand & Style

The design system is engineered to project **authority, restraint, and absolute clarity**. It serves as a digital companion for labor rights legal consultation, where the user's emotional state often ranges from anxiety to a need for rigorous verification.

The visual direction is a refined **Modern Institutional** style, blending the structural discipline of legal publishing with the accessibility of contemporary SaaS. It deliberately avoids transient trends like glassmorphism or vibrant gradients, favoring a "paper-and-ink" digital philosophy. The interface relies on generous whitespace (negative space) and precise typographic hierarchies to reduce cognitive load during high-stress legal inquiries.

## Colors

The palette is rooted in a traditional legal and academic aesthetic. 

- **Primary (Deep Ink Blue):** Used for structural elements, primary navigation, and high-level headings. It conveys stability and seriousness.
- **Success/Verify (Judicial Teal):** Reserved for validated sources, successful document uploads, and "verified" legal status markers.
- **Background (Paper White):** A slightly warm, desaturated off-white that reduces eye strain and mimics the texture of professional legal briefs.
- **Warning (Amber):** High-visibility but non-aggressive, used for time-sensitive deadlines (statutes of limitations) and moderate risks.
- **Error (Risk Red):** A muted, deep red for critical violations or high-risk contract clauses.
- **Text (Text Ink):** A dark gray-blue that maintains high contrast against the paper background while appearing softer than pure black.

## Typography

This design system uses a dual-font strategy to balance editorial elegance with functional utility.

- **Headlines (Noto Serif SC):** Used for titles, section headers, and significant pull-quotes. This serif typeface evokes the trustworthiness of printed legal codes and official gazettes.
- **Body (Noto Sans SC):** Used for all reading text, input fields, and UI controls. It ensures maximum legibility at various scales.
- **Data/Mono (JetBrains Mono):** Used for article numbers (e.g., "Article 107"), dates, case IDs, and financial calculations to provide a sense of technical precision and "fixed" legal data.

**Hierarchy Note:** Maintain strict vertical rhythm. Use the serif headlines to "anchor" pages, while using the sans-serif body for interactive elements to separate content from control.

## Layout & Spacing

The layout philosophy follows a **Fixed-Fluid Hybrid** model. On desktop, the main content is centered within a 1200px container to maintain an editorial "column" feel, preventing long line lengths that hinder reading comprehension.

- **Grid:** A 12-column grid is used for dashboards; a centered 8-column "reading lane" is used for legal articles and consultation chats.
- **Spacing Rhythm:** Based on a 4px baseline. Use `lg` (24px) for most component grouping and `2xl` (48px) for major section breaks.
- **Mobile:** Transition to a single-column layout with 16px side margins. Gutters between cards should be 12px to maximize screen real estate for text.

## Elevation & Depth

This design system rejects heavy shadows in favor of **Structural Layering**. 

1.  **Level 0 (Surface):** The Paper White (#F7F8F6) background.
2.  **Level 1 (Cards/Containers):** Pure white (#FFFFFF) backgrounds with a 1px Fog Gray (#E8ECEF) border. No shadow.
3.  **Level 2 (Interactive/Floating):** Used for dropdowns or modals. A 1px Fog Gray border paired with an extremely diffuse "Ambient Mist" shadow: `0 8px 24px rgba(23, 50, 77, 0.06)`.

Depth is primarily communicated through the contrast between the off-white background and pure white containers, creating a "stacked paper" effect.

## Shapes

The shape language is **Formal & Approachable**. 

- **Standard Elements:** Buttons, input fields, and small cards use the `Rounded` (8px) setting. This softens the professional tone just enough to be helpful rather than intimidating.
- **Large Containers:** Main content areas or modal overlays use `rounded-lg` (16px) to define major structural boundaries.
- **Status Tags:** Use a smaller 4px radius or full pill-shape depending on the context of the metadata.

## Components

### Buttons
- **Primary:** Background #17324D, Text #FFFFFF. No border. Sharp 8px corners.
- **Secondary/Outline:** Background Transparent, Text #17324D, Border 1.5px #17324D.
- **Tertiary/Ghost:** Text #17324D, no background. Used for "Cancel" or "Go Back."

### Inputs & Forms
- **Field Style:** Pure white background, 1px Fog Gray border. Focused state uses 1.5px Deep Ink Blue border. 
- **Labels:** Always Noto Sans SC, Bold, 14px. Labels sit outside the field for clarity.
- **Monospaced Data:** Use JetBrains Mono for fields capturing monetary values or specific legal article numbers.

### Cards & Lists
- **Legal Article Card:** White background, 1px border. The header of the card should use Noto Serif SC.
- **Lists:** Use 1px Fog Gray dividers between list items. Avoid zebra-striping; use whitespace to separate rows.

### Chips & Tags
- **Verified Source:** Judicial Teal (#176B68) background at 10% opacity with solid #176B68 text.
- **Risk Marker:** Risk Red (#B7473A) background at 10% opacity with solid #B7473A text.

### Feedback Elements
- **Risk Indicator:** A vertical 4px bar on the left side of a card, colored by the severity (Amber/Red), to denote the risk level of a specific contract clause.