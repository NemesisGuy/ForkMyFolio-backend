# NemesisNet Theme: LLM Cheatsheet

This document provides a comprehensive reference for generating HTML content that aligns with the NemesisNet theme's glassmorphic design system.


## Note!!
 
 do not stack or nest glass elements such as cards in sections ect.
 
### Glass Layout Rule-of-Thumb
- Use `glass-section` for prose blocks (paragraphs, quotes, headings) and feel free to keep multiple related paragraphs inside one section for flow.
- When the next block is a list/table/grid, close the `glass-section`, add a sibling `glass-card` for that structured content, then open a new section for the following prose if needed.
- Never wrap a `glass-card` inside a `glass-section`; instead stack them sequentially (section → card → section, etc.).



## 🎨 Colors & Variables

The theme uses CSS variables for consistent coloring. You can use these in inline styles if necessary, but prefer using the component classes.

| Variable | Description | Hex (Dark) |
| :--- | :--- | :--- |
| `--nemesis-blue` | Primary Brand Color | `#1E88E5` |
| `--nemesis-blue-bright` | Bright Accent | `#2979FF` |
| `--aurora-base` | Secondary Brand Color | `#00C2A8` |
| `--aurora-flare` | Bright Secondary | `#33D6B7` |
| `--nemesis-black` | Dark Background | `#0a0e27` |
| `--text-color` | Primary Text | `#F4F4F4` |
| `--text-muted` | Secondary Text | `rgba(244, 244, 244, 0.65)` |
| `--glass-bg` | Glass Effect Background | `rgba(255, 255, 255, 0.03)` |

## ✍️ Typography

The theme uses **Inter** font family.

### Headings
```html
<h1>Heading Level 1</h1>
<h2>Heading Level 2</h2>
<h3>Heading Level 3</h3>
<h4>Heading Level 4</h4>
```

### Text Styles
```html
<p>Standard paragraph text.</p>
<p class="entry-meta">Meta text style (smaller, muted color).</p>
<p class="lead">Lead paragraph text (if applicable, otherwise use standard).</p>
<a href="#">Standard Link</a>
```

### Blockquotes
```html
<blockquote>
    <p>Design is not just what it looks like and feels like. Design is how it works.</p>
    <cite>Steve Jobs</cite>
</blockquote>
```

## 🔘 Buttons

Use these classes for actions. They support both `<button>` and `<a>` tags.

```html
<!-- Primary Action (Blue Gradient) -->
<a href="#" class="btn-primary">Primary Action</a>

<!-- Ghost Action (Transparent with Border) -->
<a href="#" class="btn-ghost">Secondary Action</a>

<!-- Aurora Action (Teal/Green) -->
<a href="#" class="btn-aurora">Special Action</a>

<!-- Read More (Subtle Blue) -->
<a href="#" class="read-more">Read More</a>
```

## 🚨 Alerts & Notices

Use these for feedback messages or highlighting important information.

```html
<div class="alert alert-success">
    <i class="fas fa-check-circle"></i> <strong>Success:</strong> Operation completed.
</div>

<div class="alert alert-error">
    <i class="fas fa-exclamation-circle"></i> <strong>Error:</strong> Something went wrong.
</div>

<div class="alert alert-warning">
    <i class="fas fa-exclamation-triangle"></i> <strong>Warning:</strong> Check your inputs.
</div>

<div class="alert alert-info">
    <i class="fas fa-info-circle"></i> <strong>Info:</strong> Here is a tip.
</div>
```

## 📦 Cards & Containers

### Glass Card
Ideal for wrapping distinct content blocks like features, services, or summaries.
```html
<div class="glass-card">
    <h3>Card Title</h3>
    <p>This is a content card with the glassmorphic effect.</p>
</div>
```

### Glass Section
Use for larger, grouped content sections.
```html
<div class="glass-section">
    <h2>Section Title</h2>
    <p>This section has padding and a glass background.</p>
</div>
```

## 📋 Lists

### Standard List
```html
<ul>
    <li>List item one</li>
    <li>List item two</li>
    <li>List item three</li>
</ul>
```

### Feature List (Icon List)
A styled list with icons, perfect for "Why Choose Us" sections.
```html
<ul class="features-list">
    <li class="feature-list-item">
        <div class="feature-list-icon">
            <i class="fas fa-check"></i>
        </div>
        <div class="feature-list-content">
            <h4 class="feature-list-title">Feature Title</h4>
            <p class="feature-list-description">Description of the feature.</p>
        </div>
    </li>
    <!-- Add more items -->
</ul>
```

## 📝 Forms

Basic form styling is provided.

```html
<div class="form-group">
    <label for="exampleInput">Email Address</label>
    <input type="email" id="exampleInput" placeholder="name@example.com">
</div>

<div class="form-group">
    <label for="exampleSelect">Select Option</label>
    <select id="exampleSelect">
        <option>Option 1</option>
        <option>Option 2</option>
    </select>
</div>

<div class="form-group">
    <label for="exampleTextarea">Message</label>
    <textarea id="exampleTextarea" rows="4"></textarea>
</div>
```

## 📊 Tables

Tables are automatically styled within `glass-section` or `glass-card`.

```html
<table>
    <thead>
        <tr>
            <th>Name</th>
            <th>Role</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Reign</td>
            <td>Admin</td>
            <td>Active</td>
        </tr>
    </tbody>
</table>
```

## 🖼️ Media & Images

### Responsive Images
Images are responsive by default. Use `border-radius` via CSS or the theme's default style.
```html
<img src="image.jpg" alt="Description">
```

### Figures & Captions
```html
<figure>
    <img src="image.jpg" alt="Description">
    <figcaption>Image caption text.</figcaption>
</figure>
```

## 🛠️ Utilities

### Alignment
```html
<img src="..." class="alignleft"> <!-- Floats left -->
<img src="..." class="alignright"> <!-- Floats right -->
<img src="..." class="aligncenter"> <!-- Block center -->
```

### Grid Layouts
Use inline styles or create custom classes for grids.
```html
<!-- 2 Columns -->
<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 30px;">
    <div class="glass-card">Left</div>
    <div class="glass-card">Right</div>
</div>

<!-- Responsive Grid (Auto-fit) -->
<div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 30px;">
    <div class="glass-card">Item 1</div>
    <div class="glass-card">Item 2</div>
    <div class="glass-card">Item 3</div>
</div>
```

## 🔮 Icons

The theme includes **FontAwesome 6**. Use the `<i>` tag.
```html
<i class="fas fa-home"></i>
<i class="fas fa-user"></i>
<i class="fas fa-cog"></i>
<i class="fab fa-wordpress"></i>
```

## References / Sources List

A styled list for displaying sources or external links.

```html
<div class="references-section glass-card">
    <h3 class="references-title"><i class="fas fa-link"></i> Sources & References</h3>
    <ul class="references-list">
        <li class="reference-item">
            <a href="#" target="_blank" rel="noopener noreferrer">
                <span class="ref-title">Source Title</span>
                <span class="ref-url"><i class="fas fa-external-link-alt"></i> https://example.com</span>
            </a>
        </li>
        <li class="reference-item">
            <a href="#" target="_blank" rel="noopener noreferrer">
                <span class="ref-title">Another Source</span>
                <span class="ref-url"><i class="fas fa-external-link-alt"></i> https://google.com</span>
            </a>
        </li>
    </ul>
</div>
```
