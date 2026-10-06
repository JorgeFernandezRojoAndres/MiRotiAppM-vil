# 🥕 Rol "Administrador de Insumos" — Plan para el backend MiRoti (.NET)

> Basado en la auditoría real del código: tabla `Usuario` propia (TPH, columna `Rol` string),
> `DbInitializer.cs` para seed, MySQL (dev, Pomelo) / PostgreSQL (prod, Fly.io),
> API JWT en `ControllersApi/*` y panel web MVC con cookies por separado.

## Resumen de cambios necesarios

| # | Cambio | Archivo |
|---|--------|---------|
| 1 | Agregar `POST` y `PUT` a `IngredientesApiController` (hoy solo existe `GET`) | `ControllersApi/IngredientesApiController.cs` |
| 2 | Proteger el controlador con `[Authorize(Roles = ...)]` (hoy es público) | idem |
| 3 | Seed de un usuario con `Rol = "Administrador de Insumos"` | `DbInitializer.cs` |
| 4 | Nada más: la app móvil ya quedó ajustada a `api/ingredientes` + `costoUnitario` | — |

---

## 1. `IngredientesApiController` completo

> ✅ **Versión final basada en tu código real**: conserva tu `GET` original
> (con `unidadMedida` anidado) tal cual lo escribiste, y agrega POST/PUT con DTOs.

### DTOs de entrada (mismo archivo, no tocan la entidad `Ingrediente`)

```csharp
public class IngredienteCrearRequest
{
    public string Nombre { get; set; } = "";
    public decimal CostoUnitario { get; set; }
    public string UnidadMedida { get; set; } = ""; // abreviatura: "kg", "g", "u", "L"
}

public class IngredientePrecioRequest
{
    public decimal CostoUnitario { get; set; }
}
```

### Controlador (tu GET + POST/PUT nuevos)

```csharp
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using MiRoti.Data;

namespace MiRoti.ControllersApi
{
    [ApiController]
    [Route("api/ingredientes")]
    // 🔒 Solo el rol nuevo y el Admin; el string debe coincidir EXACTO con Usuario.Rol
    [Authorize(Roles = "Administrador de Insumos,Admin")]
    public class IngredientesApiController : ControllerBase
    {
        private readonly MiRotiContext _context;

        public IngredientesApiController(MiRotiContext context)
        {
            _context = context;
        }

        // ✅ TU MÉTODO ORIGINAL — INTACTO
        [HttpGet]
        public async Task<IActionResult> GetIngredientes()
        {
            var ingredientes = await _context.Ingredientes
                .Include(i => i.UnidadMedida)
                .Select(i => new {
                    i.Id,
                    i.Nombre,
                    i.CostoUnitario,
                    UnidadMedida = new {
                        i.UnidadMedida.Id,
                        i.UnidadMedida.Nombre,
                        i.UnidadMedida.Abreviatura
                    }
                })
                .ToListAsync();

            return Ok(ingredientes);
        }

        // ➕ NUEVO: cargar ingrediente nuevo
        [HttpPost]
        public async Task<IActionResult> CrearIngrediente([FromBody] IngredienteCrearRequest request)
        {
            if (string.IsNullOrWhiteSpace(request.Nombre))
                return BadRequest(new { mensaje = "El nombre es obligatorio." });
            if (request.CostoUnitario < 0)
                return BadRequest(new { mensaje = "El precio no puede ser negativo." });

            // Resolver la unidad por abreviatura ("kg", "g", "u", "L")
            var abrev = request.UnidadMedida?.Trim().ToLower() ?? "";
            // ⚠️ Ajustá el nombre del DbSet de unidades si difiere en tu MiRotiContext
            var unidad = await _context.UnidadesMedidas
                .FirstOrDefaultAsync(u => u.Abreviatura.ToLower() == abrev);
            if (unidad == null)
                return BadRequest(new { mensaje = "Unidad no encontrada. Usá kg, g, u o L." });

            var nombre = request.Nombre.Trim();
            if (await _context.Ingredientes.AnyAsync(i => i.Nombre.ToLower() == nombre.ToLower()))
                return Conflict(new { mensaje = "Ya existe un ingrediente con ese nombre." });

            // ⚠️ Ajustá la FK si tu entidad usa otro nombre (ej. IdUnidadMedida)
            var ingrediente = new Ingrediente
            {
                Nombre = nombre,
                CostoUnitario = request.CostoUnitario,
                UnidadMedidaId = unidad.Id
            };

            _context.Ingredientes.Add(ingrediente);
            await _context.SaveChangesAsync();

            return Ok(new { ingrediente.Id, ingrediente.Nombre, ingrediente.CostoUnitario });
        }

        // 💲 NUEVO: corregir precio (uso principal del rol)
        [HttpPut("{id:int}")]
        public async Task<IActionResult> ActualizarPrecio(int id, [FromBody] IngredientePrecioRequest request)
        {
            if (request.CostoUnitario < 0)
                return BadRequest(new { mensaje = "El precio no puede ser negativo." });

            var ingrediente = await _context.Ingredientes.FindAsync(id);
            if (ingrediente == null)
                return NotFound(new { mensaje = "Ingrediente no encontrado." });

            ingrediente.CostoUnitario = request.CostoUnitario;
            await _context.SaveChangesAsync();

            return Ok(new { ingrediente.Id, ingrediente.Nombre, ingrediente.CostoUnitario });
        }
    }
}
```

### Notas
- `UnidadMedidaId` y `_context.UnidadesMedidas` son placeholders para que ajustes a
  los nombres reales de tu `MiRotiContext` y de la entidad `Ingrediente`.
- ⚠️ **Ojo con el rollback de precios:** al sobrescribir `CostoUnitario` cambia el costo
  de los platos vía `PlatoIngrediente`. Si más adelante quieren auditoría, agregar tabla
  `HistorialPreciosIngredientes` y un insert en el `PUT` — no cambia el contrato con la app.

## 2. Seed del usuario en `DbInitializer.cs`

La tabla `Usuario` es propia (TPH, columna `Rol` string). No hay tabla de roles:
el rol nuevo es solo un string nuevo. Agregar al initializer, **sin tocar** los
usuarios existentes:

```csharp
// 🥕 Usuario responsable de precios de ingredientes
const string rolInsumos = "Administrador de Insumos";

if (!await context.Usuarios.AnyAsync(u => u.Email == "insumos@miroti.com"))
{
    context.Usuarios.Add(new Usuario
    {
        Email = "insumos@miroti.com",
        PasswordHash = passwordHasher.HashPassword(null, "insumos123"),
        Rol = rolInsumos,
        TipoUsuario = "..." // el mismo valor TPH que usen los demás usuarios
    });
    await context.SaveChangesAsync();
}
```

> Ajustar `TipoUsuario` y `PasswordHasher` a como lo hacen hoy los seeds existentes
> (`admin@miroti.com`, etc.). **No usar `EnsureCreated()` en producción:** como ya
> usan Migraciones EF, cualquier cambio futuro de modelo debe ir con
> `dotnet ef migrations add ... && dotnet ef database update`.
> (Para este plan no hace falta migración: no se agregan columnas ni tablas.)

## 3. Cadena completa del rol (verificar en orden)

1. **DB:** `Usuario.Rol = "Administrador de Insumos"` (string exacto).
2. **Login:** `AuthService` → `NormalizarRol` **no toca** roles nuevos (solo mapea
   `"Administrador"` → `"Admin"`), así que el claim sale tal cual. ✅ No tocar.
3. **Body del login:** ya devuelve `{ token, id, email, rol }` con clave `rol`. ✅ No tocar.
4. **Anotaciones:** `[Authorize(Roles = "Administrador de Insumos,Admin")]` en el controlador.
5. **Si el rol necesita ver platos:** agregar el rol a `PlatosApiController`:
   `[Authorize(Roles = "Cliente,Cadete,Administrador de Insumos")]`. (La app hoy
   no le muestra platos; opcional.)

## 4. Panel web MVC (opcional, después)

El panel usa cookies + Razor y es independiente de la API. Para administrar el rol
nuevo desde el panel alcanza con el ABM de usuarios existente: agregar la opción
del string `"Administrador de Insumos"` en el combo/lista de roles.

## 5. Contrato con la app móvil (ya implementado en este repo)

| Método | Ruta | Auth | Body / Respuesta |
|--------|------|------|------------------|
| GET  | `api/ingredientes`       | JWT (rol nuevo o Admin) | `[{ id, nombre, costoUnitario, unidadMedida: { id, nombre, abreviatura } }]` |
| POST | `api/ingredientes`       | JWT | `{ nombre, costoUnitario, unidadMedida: "kg" }` |
| PUT  | `api/ingredientes/{id}`  | JWT | `{ costoUnitario }` |

Notas de compatibilidad:
- El modelo Android lee `costoUnitario` (con aliases `CostoUnitario`, `precio`, `Precio`)
  y `unidadMedida` como **objeto anidado** para el GET.
- Para POST/PUT la app usa DTOs propios (`IngredienteCrearRequest` con abreviatura
  string, `IngredientePrecioRequest` con solo el precio) que calzan con los del backend.
- Los errores del backend (`{ "mensaje": "..." }`) se muestran directo en el formulario
  (ej. 409 duplicado, 400 unidad inválida).

## 6. Checklist de no-regresión (con los usuarios sembrados)

- [ ] `admin@miroti.com` / `admin123` (Admin) → login OK en app, sigue igual.
- [ ] `juan@mail.com` / `1234` (Cliente) → login OK, menú de cliente intacto.
- [ ] `pedro@mail.com` / `1234` (Cadete) → login OK, entregas intactas.
- [ ] `chef@miroti.com` / `123456` (Cocinero) → sigue bloqueado en la app móvil
      ("Acceso restringido"), igual que hoy. No es regresión: es el comportamiento actual.
- [ ] `insumos@miroti.com` / `insumos123` (nuevo) → entra directo a Insumos,
      puede cargar ingredientes y corregir precios.
- [ ] `GET api/ingredientes` ya NO es público (401 sin token).
- [ ] Usuario con token de Cliente intentando `PUT api/ingredientes/{id}` → 403.
