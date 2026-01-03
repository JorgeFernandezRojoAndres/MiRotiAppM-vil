
# MVVM Clean Architecture Rules

## Overview
Custom rules for Android projects using MVVM (Model-View-ViewModel) clean architecture pattern with Java or Kotlin.

## Rules

### 1. Logic Separation - No Logic in UI Components
**Severity:** Error

Fragments and Activities must not contain:
- `if/else` statements for business logic
- `try/catch` blocks
- `throw` statements
- Data validations
- Flow control logic

**All logic must reside in the ViewModel.**

```kotlin
// ❌ Incorrect
class ProfileFragment : Fragment() {
    fun loadUser() {
        try {
            val user = apiCall()
            if (user != null) {
                displayUser(user)
            }
        } catch (e: Exception) {
            showError(e.message)
        }
    }
}

// ✅ Correct
class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by viewModels()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.user.observe(viewLifecycleOwner) { user ->
            displayUser(user)
        }
    }
}
```

### 2. ViewBinding Requirement
**Severity:** Error

Use ViewBinding instead of `findViewById()`:

```kotlin
// ❌ Incorrect
val textView = findViewById<TextView>(R.id.text_view)

// ✅ Correct
private lateinit var binding: FragmentProfileBinding

override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View = FragmentProfileBinding.inflate(inflater, container, false).also {
    binding = it
}.root
```

### 3. ViewModel Extension
**Severity:** Error

ViewModels must extend `AndroidViewModel` and access context via `getApplication()`:

```kotlin
// ✅ Correct
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Context>()
}
```

### 4. No Context Passing to ViewModel
**Severity:** Error

Never pass or store `Context` from UI layers to ViewModel:

```kotlin
// ❌ Incorrect
viewModel.setContext(this)

// ✅ Correct
// Use getApplication() in ViewModel if needed
```

### 5. Observation Pattern Only
**Severity:** Error

Views must only observe `LiveData` from ViewModel. Never execute:
- Network requests
- Database queries
- Business logic

```kotlin
// ✅ Correct
viewModel.userData.observe(viewLifecycleOwner) { data ->
    updateUI(data)
}
```

### 6. Method Visibility in ViewModel
**Severity:** Warning

- Internal/helper methods: `private`
- Methods called from UI: `public`

```kotlin
// ✅ Correct
class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    fun loadUserProfile() { } // Public - called from Fragment
    private fun validateData() { } // Private - internal logic
}
```

### 7. No Return Values from ViewModel Methods
**Severity:** Error

ViewModel methods must not return values. Use `MutableLiveData` for results:

```kotlin
// ❌ Incorrect
fun getUserData(): User { }

// ✅ Correct
val userData = MutableLiveData<User>()
fun loadUserData() {
    userData.value = fetchedUser
}
```

### 8. Architecture Pattern
**Severity:** Info

Follow the established pattern where:
- **Fragment/Activity:** Observes and updates UI
- **ViewModel:** Handles all logic, validation, and state
- **Repository:** Manages data sources
- **Model:** Data classes
