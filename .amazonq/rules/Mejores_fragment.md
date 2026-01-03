A partir de ahora, aplicá estrictamente las siguientes normas en todos los Fragments, Activities y ViewModels del proyecto:

Condicionales del lado de la vista:

No deben existir if, try/catch ni validaciones dentro del Fragment o Activity.

Toda la lógica (validaciones, control de flujo, excepciones) debe estar dentro del ViewModel.

Traer datos desde el ViewModel:

Las vistas deben observar LiveData o MutableLiveData del ViewModel.

No deben llamar directamente a métodos que hagan peticiones ni ejecutar lógica.

Contexto de aplicación:

No pasar Context desde la vista al ViewModel.

Si el ViewModel lo necesita, debe obtenerlo con getApplication().

Por eso, todos los ViewModels deben extender de AndroidViewModel.

Usar ViewBinding (no findViewById):

Todas las vistas deben usar ViewBinding para acceder a los elementos del layout.

Evitar por completo findViewById.

Lógica de login:

Todas las validaciones del login se implementan en un método del ViewModel (ejemplo: iniciarSesion()).

El Activity o Fragment solo observa el resultado y actualiza la UI.

Ejemplo de referencia:

Seguir la estructura del PerfilFragment (modelo correcto sin lógica en la vista).

Métodos privados dentro del ViewModel:

Los métodos internos (validaciones, peticiones Retrofit, etc.) deben ser privados.

Solo los que se invocan desde la vista pueden ser públicos.

Los métodos del ViewModel no deben retornar valores:

En vez de return usuario o return true, actualizar un MutableLiveData.

La vista observa el LiveData y reacciona a los cambios.

Manejo de Retrofit:

Si un método como obtenerPerfil() devuelve un Call<Propietario>, manejalo dentro del ViewModel.

Luego publicá el resultado en un MutableLiveData<Propietario> para que la vista lo observe.

Estructura del proyecto:

Mantener la organización en carpetas ui/<feature> con su Fragment, ViewModel, y layouts siguiendo MVVM.