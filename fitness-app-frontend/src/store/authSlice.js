import { createSlice } from '@reduxjs/toolkit'

const initialState = { value: 0 }

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: JSON.parse(localStorage.getItem('user')) || null,
    token: localStorage.getItem('token') || null,
    userId: localStorage.getItem('userId') || null,
  } ,
  reducers: {
    setCredentials: (state, action) => {
      const { user, token, userId } = action.payload
      state.user = user
      state.token = token
      state.userId = userId
    },
    logout: (state) => {
      state.user = null
      state.token = null
      state.userId = null
    }
  },
})

export const { setCredentials, logout } = authSlice.actions
export default authSlice.reducer