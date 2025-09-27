package com.example.lab2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val travelModel: TravelModel,
    private val currentUser: UserModel,
    private val repository: FirestoreTravelRepository
) : ViewModel() {

    val userId = currentUser.id

    private val _suggestedTrips = MutableStateFlow<List<Travel>>(emptyList())
    val suggestedTrips: StateFlow<List<Travel>> = _suggestedTrips

    private val _someTrips = MutableStateFlow<List<Travel>>(emptyList())
    val someTrips: StateFlow<List<Travel>> = _someTrips

    private val _myProposals = MutableStateFlow<List<Travel>>(emptyList())
    val myProposals: StateFlow<List<Travel>> = _myProposals

    private val _friendsProposals = MutableStateFlow<List<Travel>>(emptyList())
    val friendsProposals: StateFlow<List<Travel>> = _friendsProposals

    init {
        fetchSuggestedTrips()
        fetchMyProposals()
        fetchFriendsProposals()
        fetchSomeTrips()
    }

    private fun fetchSuggestedTrips() {
        viewModelScope.launch {
            val suggested = repository.getSuggestedTravels()
            _suggestedTrips.value = suggested
        }
    }

    private fun fetchSomeTrips() {
        viewModelScope.launch {
            val suggested = repository.getAllTravels()
            _someTrips.value = suggested
        }
    }

    private fun fetchMyProposals() {
        viewModelScope.launch {
            val proposals = repository.getProposalsForUser(currentUser.id)
            _myProposals.value = proposals.first()
        }
    }

    private fun fetchFriendsProposals() {
        viewModelScope.launch {
            val friendsProposals = repository.getFriendsProposals()
            _friendsProposals.value = friendsProposals
        }
    }

}