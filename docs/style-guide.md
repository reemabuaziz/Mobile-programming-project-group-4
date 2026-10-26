# Project Material Exchange — UI Style Guide

## Purpose
This guide records the visual decisions used in the Stage 1 Figma prototype so the UI can be implemented consistently.

## Color palette
- Primary green: `#387C3E` — active navigation labels, account links, and positive actions.
- Accent dusty rose: `#DE9C9D` — Add (+) button and selected/attention surfaces.
- Blush surface: `#F9EFEE` — cards, input fields, and secondary surfaces.
- Muted rose: `#E2D4D3` — supporting surfaces.
- Success: `#387C3E`.
- Error/reject: `#EA6068`.
- Text: `#000000` on `#FFFFFF` for strong contrast.

## Typography
Use a sans-serif typeface; Arial is the implementation target. Screen titles are 24 sp bold, section headings 20 sp bold, body text 16 sp, and control labels 14 sp.

## Spacing and shape
Use an 8 dp spacing grid, 16 dp outer screen padding, 12 dp rounded card/field corners, and touch targets of at least 48 dp.

## Components
Use rounded text fields, buttons, cards, category chips, section selectors, a floating Add button, and bottom navigation. Planned Jetpack Compose equivalents include `TextField`, `Button`, `Card`, `FilterChip`, `TabRow`, `FloatingActionButton`, and `NavigationBar`.

## Interaction and status
- Keep selected tab/category states visually explicit.
- Use green for success/accept and red for reject/error.
- Display status words (Available, Pending, Reserved, Completed) as text; meaning must not rely on color alone.
- Confirm destructive actions such as signing out or discarding an edited form.
- Keep spacing, title hierarchy, control sizes, and button treatment consistent across screens.

## Accessibility and quality notes
- Maintain readable contrast on pale buttons and inputs.
- Use consistent typography across all eight core screens.
- Keep controls comfortably tappable and labels unambiguous.
- Use clear copy: “Available Materials” and “Forgot password?”.
